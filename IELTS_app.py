# Import necessary libraries
import streamlit as st
import pandas as pd
import docx
import nltk
import numpy as np
import faiss
from sentence_transformers import SentenceTransformer
from groq import Groq

# Load the required model and tools
nltk.download('punkt')

# Initialize the models used from the previous code
model = SentenceTransformer("dunzhang/stella_en_400M_v5", trust_remote_code=True).cuda()
GROQ_API_KEY = "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"
llm = Groq(model="llama-3.1-8b-instant", api_key=GROQ_API_KEY)

# Load previously created FAISS index and embeddings
index = faiss.read_index('faiss_product_index.bin')
product_embeddings = np.load('product_embeddings_faiss.npy')

# Streamlit UI
st.title("IELTS Writing Task 1 & 2 Evaluation")

# Instructions
st.markdown("""
### Instructions for IELTS Writing Evaluation
Please upload your IELTS Writing Task 1 or 2 as a text file (.txt) or Word document (.doc or .docx).
We will analyze your text based on the IELTS writing evaluation criteria:
- Task Achievement
- Coherence and Cohesion
- Lexical Resource
- Grammatical Range and Accuracy

The application will score your writing, provide feedback, and suggest improvements.
""")

# File uploader for IELTS Writing Input
uploaded_file = st.file_uploader("Upload your Writing Task (txt, doc, docx)", type=["txt", "doc", "docx"])

# Function to read file content
def read_file(file):
    if file.name.endswith(".txt"):
        content = file.read().decode("utf-8")
    elif file.name.endswith(".docx"):
        doc = docx.Document(file)
        content = "\n".join([para.text for para in doc.paragraphs])
    else:
        content = ""
    return content

# Main Evaluation Function
def evaluate_writing(text):
    # Splitting the text into sentences for more granular analysis
    sentences = nltk.sent_tokenize(text)
    
    # Generating writing details using Llama model
    prompt = f"Assess the quality of the following IELTS writing task based on Task Achievement, Coherence and Cohesion, Lexical Resource, and Grammatical Range and Accuracy: {text}"
    generated_details = llm.complete(prompt=prompt)
    
    # Combine the writing content and generated details for embedding
    combined_query = text + "," + generated_details

    # Embed the combined query
    combined_query_embedding = model.encode([combined_query]).astype('float32')

    # Perform Faiss search for the nearest neighbors
    D, I = index.search(combined_query_embedding, 1)  # D is distances, I is indices of the nearest neighbor

    # Calculating the overall score based on distance
    overall_score = 10 - (D[0][0] * 10)  # Assuming a distance-to-score conversion

    # Generating feedback using the Llama model
    feedback = llm.complete(prompt=f"Provide detailed feedback on the following IELTS writing task: {text}")
    return overall_score, feedback

# Processing the uploaded file
if uploaded_file is not None:
    # Read the content of the file
    writing_content = read_file(uploaded_file)
    
    # Display the content to the user
    st.subheader("Uploaded Writing Task Content")
    st.write(writing_content)
    
    # Evaluate the content
    st.subheader("Evaluation Results")
    score, feedback = evaluate_writing(writing_content)
    
    # Display the score
    st.write(f"**Overall Score:** {score:.2f}/10")
    
    # Display the feedback
    st.subheader("Feedback and Suggested Improvements")
    st.write(feedback)
    
    # Option to download corrected version
    corrected_text = llm.complete(prompt=f"Correct the following IELTS writing task: {writing_content}")
    st.subheader("Corrected Version")
    st.write(corrected_text)
    
    # Provide download link for corrected version
    st.download_button("Download Corrected Version", corrected_text, file_name="corrected_version.txt")

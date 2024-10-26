from openai import OpenAI
import streamlit as st
import random

# Instantiate OpenAI client with your API key
api_key = st.secrets["OPENAI_API_KEY"]
api_key = api_key['OPENAI_API_KEY']
client = OpenAI(api_key=api_key)

def openai_chat(messages):
    """
    Function to interact with OpenAI API and get responses for improving IELTS writing.
    """
    response = client.chat.completions.create(
        model="gpt-4o-mini",
        messages=messages
    )
    return response.choices[0].message.content

# Initialize chat history in session state
if "messages" not in st.session_state:
    st.session_state["messages"] = [
        {"role": "system", "content": "You are an IELTS writing assistant. You will help users by generating prompts, analyzing their writing, providing feedback, giving a score out of 9, offering suggestions for improvement, and providing a corrected version of their text."}
    ]

st.title("IELTS Writing Checker for Task 2")
st.write("This tool helps improve IELTS writing by generating prompts, providing corrections, changes, feedback, and a score out of 9 based on IELTS criteria.")

# Step 1: Generate a Prompt for Task 2
if st.button("Generate Task 2 Prompt"):
    prompt_message_content = openai_chat([
        {"role": "system", "content": "You are an assistant that generates unique IELTS Task 2 prompts."},
        {"role": "user", "content": "Generate a unique IELTS Task 2 writing prompt."}
    ])
    prompt_message = {"role": "assistant", "content": f"Write an essay of at least 250 words on the following topic: '{prompt_message_content}'"}
    st.session_state["messages"] = [
        st.session_state["messages"][0],  # Keep the system message
        prompt_message
    ]
    st.write("### Task 2 Prompt:")
    st.write(prompt_message["content"])

# Chat input and display
user_input = st.chat_input("Paste your IELTS writing here:")
if user_input:
    st.session_state["messages"].append({"role": "user", "content": user_input})

    with st.spinner("Analyzing your writing..."):
        # Combined request for feedback, scoring, improvement suggestions, and corrected version
        combined_request = {"role": "user", "content": "Please analyze the writing, provide detailed feedback on content, coherence, grammar, and vocabulary, give a score out of 9 based on IELTS criteria (Task Achievement, Coherence and Cohesion, Lexical Resource, Grammatical Range and Accuracy), provide suggestions on how to improve this essay to achieve a higher score, and provide a corrected version of the essay."}
        st.session_state["messages"].append(combined_request)
        combined_response = openai_chat(st.session_state["messages"])
        st.session_state["messages"].append({"role": "assistant", "content": combined_response})

    # Display only the final combined response
    st.markdown(st.session_state["messages"][-1]["content"])

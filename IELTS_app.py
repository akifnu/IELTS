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
        {"role": "system", "content": "You are an IELTS writing assistant. You will help users by generating prompts, analyzing their writing, providing feedback, giving a score out of 9, and offering suggestions for improvement."}
    ]

st.title("IELTS Writing Checker for Task 2")
st.write("This tool helps improve IELTS writing by generating prompts, providing corrections, changes, feedback, and a score out of 9 based on IELTS criteria.")

# Step 1: Generate a Prompt for Task 2
prompts = [
    "Some people believe that increasing the price of fuel is the best way to solve environmental problems. To what extent do you agree or disagree with this statement?",
    "Many people think that social media platforms are having a negative effect on both individuals and society. Do you agree or disagree?",
    "Some believe that advancements in artificial intelligence will lead to more harm than good. Discuss both views and give your opinion.",
    "In some countries, the average weight of people is increasing, and their levels of health and fitness are decreasing. What do you think are the causes of these problems and what measures could be taken to solve them?",
    "Some people think that all university students should study whatever they like, while others believe that they should only be allowed to study subjects that will be useful in the future, such as those related to science and technology. Discuss both views and give your opinion."
]

if st.button("Generate Task 2 Prompt"):
    prompt_message_content = random.choice(prompts)
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
        # Combined request for feedback, scoring, and improvement suggestions
        combined_request = {"role": "user", "content": "Please analyze the writing, provide detailed feedback on content, coherence, grammar, and vocabulary, give a score out of 9 based on IELTS criteria (Task Achievement, Coherence and Cohesion, Lexical Resource, Grammatical Range and Accuracy), and provide suggestions on how to improve this essay to achieve a higher score."}
        st.session_state["messages"].append(combined_request)
        combined_response = openai_chat(st.session_state["messages"])
        st.session_state["messages"].append({"role": "assistant", "content": combined_response})

    # Display the full conversation
    for message in st.session_state["messages"]:
        with st.chat_message(message["role"]):
            st.markdown(message["content"])

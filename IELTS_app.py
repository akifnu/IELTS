import openai
import streamlit as st
import os

# Load API Key securely from Streamlit secrets
openai.api_key = "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"

def openai_chat(messages):
    """
    Function to interact with OpenAI API and get responses for improving IELTS writing.
    """
    response = openai.ChatCompletion.create(
        model="gpt-4",
        messages=messages
    )
    return response['choices'][0]['message']['content']

# Initialize chat history in session state
if "messages" not in st.session_state:
    st.session_state["messages"] = [
        {"role": "system", "content": "You are an IELTS writing assistant."}
    ]

st.title("IELTS Writing Checker with GPT-4")
st.write("This tool helps improve IELTS writing by providing corrections, changes, and feedback.")

# Chat input and display
user_input = st.chat_input("Paste your IELTS writing here:")
if user_input:
    # Add user input to chat history
    st.session_state["messages"].append({"role": "user", "content": user_input})

    # Generate response from OpenAI
    with st.spinner("Processing..."):
        response = openai_chat(st.session_state["messages"])
        st.session_state["messages"].append({"role": "assistant", "content": response})

    # Display chat history
    for message in st.session_state["messages"]:
        with st.chat_message(message["role"]):
            st.markdown(message["content"])


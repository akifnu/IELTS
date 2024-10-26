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
sample_prompts = [
    "Some people believe that increasing the price of fuel is the best way to solve environmental problems. To what extent do you agree or disagree with this statement?",
    "Many people think that social media platforms are having a negative effect on both individuals and society. Do you agree or disagree?",
    "Some believe that advancements in artificial intelligence will lead to more harm than good. Discuss both views and give your opinion.",
    "In some countries, the average weight of people is increasing, and their levels of health and fitness are decreasing. What do you think are the causes of these problems and what measures could be taken to solve them?",
    "Some people think that all university students should study whatever they like, while others believe that they should only be allowed to study subjects that will be useful in the future, such as those related to science and technology. Discuss both views and give your opinion.",
    "In many cities, an increasing number of people do not know their neighbors. What do you think causes this problem? What can be done to improve this situation?",
    "Some people think that parents should teach children how to be good members of society. Others, however, believe that school is the best place to learn this. Discuss both views and give your opinion.",
    "In some countries, people spend long hours at work. Why does this happen? Is it a positive or negative development?",
    "Nowadays many people have access to computers on a wide basis and a large number of children play computer games. What are the positive and negative impacts of playing computer games and what can be done to minimize the bad effects?",
    "Some people think that it is more important to plant trees in open areas of towns and cities than to build more housing. To what extent do you agree or disagree?"
]

if st.button("Generate Task 2 Prompt"):
    prompt_message_content = openai_chat([
        {"role": "system", "content": "You are an assistant that generates unique IELTS Task 2 prompts based on the following examples."},
        {"role": "user", "content": f"Generate a unique IELTS Task 2 writing prompt similar to these examples: {sample_prompts}"}
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

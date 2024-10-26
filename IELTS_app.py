from openai import OpenAI
import streamlit as st

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
if st.button("Generate Task 2 Prompt"):
    prompt_message = {"role": "assistant", "content": "Write an essay of at least 250 words on the following topic: 'Some people believe that increasing the price of fuel is the best way to solve environmental problems. To what extent do you agree or disagree with this statement?'"}
    st.session_state["messages"].append(prompt_message)
    st.write("### Task 2 Prompt:")
    st.write(prompt_message["content"])

# Chat input and display
user_input = st.chat_input("Paste your IELTS writing here:")
if user_input:
    st.session_state["messages"].append({"role": "user", "content": user_input})

    with st.spinner("Analyzing your writing..."):
        # Step 2: Analyze the Writing and Provide Feedback
        feedback_request = {"role": "user", "content": "Please analyze the writing, provide detailed feedback on content, coherence, grammar, and vocabulary."}
        st.session_state["messages"].append(feedback_request)
        feedback_response = openai_chat(st.session_state["messages"])
        st.session_state["messages"].append({"role": "assistant", "content": feedback_response})

        # Step 3: Score the Essay Out of 9 Based on IELTS Criteria
        score_request = {"role": "user", "content": "Give a score out of 9 for this essay based on the IELTS criteria (Task Achievement, Coherence and Cohesion, Lexical Resource, Grammatical Range and Accuracy)."}
        st.session_state["messages"].append(score_request)
        score_response = openai_chat(st.session_state["messages"])
        st.session_state["messages"].append({"role": "assistant", "content": score_response})

        # Step 4: Provide Suggestions for Improvement
        improvement_request = {"role": "user", "content": "Please provide suggestions on how to improve this essay to achieve a higher score."}
        st.session_state["messages"].append(improvement_request)
        improvement_response = openai_chat(st.session_state["messages"])
        st.session_state["messages"].append({"role": "assistant", "content": improvement_response})

    # Display the full conversation
    for message in st.session_state["messages"]:
        with st.chat_message(message["role"]):
            st.markdown(message["content"])

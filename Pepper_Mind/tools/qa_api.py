import requests
import random
import uuid

def get_qa_answer(question):
    url = "https://abdelhadi-hireche.steamship.run/qa-bot-telegram-j21/qa-bot-telegram-j21/answer"
    headers = {
        "Content-Type": "application/json",
        "Authorization": "Bearer 26721291-D416-4C30-AA6A-304FC4E0BC5F"
    }
    data = {
        "question": question
    }

    response = requests.post(url, headers=headers, json=data)

    answer = response.json()['answer']

    bq = Block(text=answer)

# Example usage:
question = "when is final exams"
answer = get_qa_answer(question)
print(answer)

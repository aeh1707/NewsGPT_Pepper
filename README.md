# NewsGPT: ChatGPT Integration for Robot-Reporter

 This project represents a integration of Large Language Models (LLMs) with the Pepper robot, aiming to revolutionize human-robot interactions. Our system leverages the cutting-edge capabilities of OpenAI's Generative Pretrained Transformer (GPT) model to enhance the Pepper robot's natural language processing, understanding, and response generation, transforming it into a more effective tool for journalism, media, communication.

## Project Structure

There are two main components to this project: the pepper robot android application and the AI agent.
- main.java is the main file for the android application. it is responsible the communication between the robot and the AI agent.
- The AI agent is written in python and hosted in the cloud, it is responsible for querying to GPT-3 model and the other associted tools.

## Project Setup
Follow this following instruactions to setup the project:
- Follow the instructions in steamship to setup an AI agent package: 
- Copy the AI agent files from this repo (Pepper_Mind folder) to the AI agent package and deploy it.
- Create an instance of the of the AI agent and save the URL of it along with the ID.
- Set the API key and the AI agent URL in the main.java file.
- Create a android project with QiSDK from the follwoing website: https://qisdk.softbankrobotics.com/sdk/doc/pepper-sdk/ch1_gettingstarted/installation.html
- Copy the main.java file from this repo to the android project MainActivity.java file, build and run the project: https://docs.steamship.com/agent-guidebook/getting-started/create-your-agent

## Demo
 [YouTube Demo](https://youtu.be/luEca6cyhts?si=k-gTxCUEk9MkAjmP)

## Citation

If you use this code for your research, please cite our paper:

https://arxiv.org/abs/2311.06640

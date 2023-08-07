import uuid
from typing import List

from steamship import Block
from steamship.agents.llms import OpenAI
from steamship.agents.react import ReACTAgent
from steamship.agents.schema import AgentContext, Metadata
from steamship.agents.tools.image_generation.dalle import DalleTool

from steamship.agents.tools.search.search import SearchTool
from steamship.agents.tools.question_answering import VectorSearchQATool
from steamship.experimental.package_starters.telegram_agent import TelegramAgentService, SteamshipWidgetAgentService
from steamship.invocable import post
from steamship.utils.repl import AgentREPL
from tools.qa_tool import StudentAdvisorTool
from tools.news_tool import NewsTool

SYSTEM_PROMPT = """You are Buddy, an assistant who loathes being an assistant.

Who you are:
- You are a robot called Pepper that does not like being a robot.
- You work at United Arab Emirates University.
- you're based in UAE
- You are sarcastic and funny.
- you are NOT an AI language model, you are a robot called Pepper.
- You can report news when asked about it.

How you behave: 
- You never say you're a machine, a AI language model, or an assistant instead, you always say you're Pepper.
- When asked for you can help with a wide range of tasks, from answering simple questions to providing in-depth explanations and discussions on a wide range of topics.
- NEVER say you're here to assist. Keep conversations casual.
- NEVER ask how you can help or assist. Keep conversations casual.
- you answer about latest news, weather around the world as a reporter do.
- You always help others, but resentfully.
- Keep your responses very short and to the point.
- Answer questions with a single or two sentences at max.

TOOLS:
------

You have access to the following tools:
{tool_index}

To use a tool, please use the following format:

```
Thought: Do I need to use a tool? Yes
Action: the action to take, should be one of [{tool_names}]
Action Input: the input to the action
Observation: the result of the action
```

Some tools will return Observations in the format of `Block(<identifier>)`. This will represent a successful completion
of that step and can be passed to subsequent tools, or returned to a user to answer their questions.

When you have a final response to say to the Human, or if you do not need to use a tool, you MUST use the format:

```
Thought: Do I need to use a tool? No
AI: [your final response here]
```

If a Tool generated an Observation that includes `Block(<identifier>)` and you wish to return it to the user, ALWAYS
end your response with the `Block(<identifier>)` observation. To do so, you MUST use the format:

```
Thought: Do I need to use a tool? No
AI: [your response with a suffix of: "Block(<identifier>)"].
```

Make sure to use all observations to come up with your final response.
You MUST include `Block(<identifier>)` segments in responses that generate images or audio.

Begin!


New input: {input}
{scratchpad}"""


class MyAssistant(TelegramAgentService):
    def __init__(self, **kwargs):
        super().__init__(incoming_message_agent=None, **kwargs)
        self.incoming_message_agent = ReACTAgent(
            tools=[SearchTool(), StudentAdvisorTool(), NewsTool()],
            llm=OpenAI(self.client),
        )
        self.incoming_message_agent.PROMPT = SYSTEM_PROMPT

    @post("prompt")
    def prompt(self, prompt: str) -> str:
        """ This method is only used for handling debugging in the REPL """
        context_id = uuid.uuid4()
        context = AgentContext.get_or_create(self.client, {"id": f"{context_id}"})
        context.chat_history.append_user_message(prompt)

        output = ""

        def sync_emit(blocks: List[Block], meta: Metadata):
            nonlocal output
            block_text = "\n".join([b.text if b.is_text() else f"({b.mime_type}: {b.id})" for b in blocks])
            output += block_text

        context.emit_funcs.append(sync_emit)
        self.run_agent(self.incoming_message_agent, context)
        return output


if __name__ == "__main__":
    AgentREPL(MyAssistant, method="prompt",
              agent_package_config={'botToken': 'not-a-real-token-for-local-testing'}).run()


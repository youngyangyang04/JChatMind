import React from "react";
import type { ChatMessageVO } from "../api/agentApi.ts";
import { Bubble } from "@ant-design/x";

interface AgentChatHistoryProps {
  messages: ChatMessageVO[];
}

const AgentChatHistory: React.FC<AgentChatHistoryProps> = ({ messages }) => {
  if (messages.length === 0) {
    return <></>;
  }
  
  return (
    <div>
      {messages.map((message) => {
        return (
          <div className="mb-1">
            {
              message.role === "assistant"
            }
            <Bubble
              content={message.content}
              key={message.id}
              placement={message.role === "user" ? "end" : "start"}
            />
          </div>
        );
      })}
    </div>
  );
};

export default AgentChatHistory;

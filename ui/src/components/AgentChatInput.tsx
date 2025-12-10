import React from "react";
import { Sender } from "@ant-design/x";

interface AgentChatInputProps {
  onSend: (message: string) => void;
}

const AgentChatInput: React.FC<AgentChatInputProps> = ({ onSend }) => {
  return <Sender onSubmit={onSend} placeholder="输入消息..." />;
};

export default AgentChatInput;

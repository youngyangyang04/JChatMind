import React from "react";
import { Card, Space, Typography } from "antd";
import {
  BulbOutlined,
  MessageOutlined,
  RobotOutlined,
} from "@ant-design/icons";
import { Sender } from "@ant-design/x";

const { Title, Text } = Typography;

interface DefaultAgentChatViewProps {
  handleSendMessage: (message: string) => void;
  loading: boolean;
}

const DefaultAgentChatView: React.FC<DefaultAgentChatViewProps> = ({
  handleSendMessage,
  loading,
}) => {
  return (
    <div className="flex flex-col h-full">
      <div className="flex-1 flex items-center justify-center p-6">
        <div className="max-w-2xl w-full space-y-6">
          <div className="text-center mb-8">
            <Title level={2} className="mb-2">
              开始新的对话
            </Title>
            <Text type="secondary" className="text-base">
              选择一个智能体助手开始聊天，或直接发送消息创建新会话
            </Text>
          </div>

          <Space direction="vertical" size="large" className="w-full">
            <Card
              hoverable
              className="cursor-pointer transition-all hover:shadow-lg"
            >
              <Space size="middle">
                <div className="w-12 h-12 rounded-full bg-gradient-to-br from-blue-400 to-purple-400 flex items-center justify-center">
                  <RobotOutlined className="text-white text-xl" />
                </div>
                <div>
                  <Title level={5} className="mb-1">
                    智能对话
                  </Title>
                  <Text type="secondary">
                    与 AI 助手进行智能对话，获取帮助和建议
                  </Text>
                </div>
              </Space>
            </Card>

            <Card
              hoverable
              className="cursor-pointer transition-all hover:shadow-lg"
            >
              <Space size="middle">
                <div className="w-12 h-12 rounded-full bg-gradient-to-br from-green-400 to-teal-400 flex items-center justify-center">
                  <BulbOutlined className="text-white text-xl" />
                </div>
                <div>
                  <Title level={5} className="mb-1">
                    知识问答
                  </Title>
                  <Text type="secondary">
                    基于知识库进行问答，获取准确的信息
                  </Text>
                </div>
              </Space>
            </Card>

            <Card
              hoverable
              className="cursor-pointer transition-all hover:shadow-lg"
            >
              <Space size="middle">
                <div className="w-12 h-12 rounded-full bg-gradient-to-br from-orange-400 to-red-400 flex items-center justify-center">
                  <MessageOutlined className="text-white text-xl" />
                </div>
                <div>
                  <Title level={5} className="mb-1">
                    快速开始
                  </Title>
                  <Text type="secondary">
                    在下方输入框输入消息，立即开始对话
                  </Text>
                </div>
              </Space>
            </Card>
          </Space>
        </div>
      </div>
      <div className="border-t border-gray-200 p-4 bg-white">
        <Sender
          onSubmit={handleSendMessage}
          loading={loading}
          placeholder="输入消息开始对话..."
        />
      </div>
    </div>
  );
};

export default DefaultAgentChatView;

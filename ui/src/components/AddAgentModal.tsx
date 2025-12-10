import React, { useEffect, useState } from "react";
import { Button, Checkbox, Input, Modal, Select, Slider } from "antd";
import TextArea from "antd/es/input/TextArea";
import { SaveOutlined } from "@ant-design/icons";
import {
  type CreateAgentRequest,
  type UpdateAgentRequest,
  type AgentVO,
  type ModelType,
  getOptionalTools,
  type ToolVO,
} from "../api/agentApi.ts";
import { useKnowledgeBases } from "../hooks/useKnowledgeBases.ts";

interface AddAgentModalProps {
  open: boolean;
  onClose: () => void;
  createAgentHandle: (request: CreateAgentRequest) => Promise<void>;
  updateAgentHandle?: (
    agentId: string,
    request: UpdateAgentRequest,
  ) => Promise<void>;
  editingAgent?: AgentVO | null;
}

const menuItems = [
  { key: "base", label: "基础设置" },
  { key: "model", label: "模型设置" },
  { key: "knowledge", label: "知识库设置" },
  // { key: "mcp", label: "MCP 服务器" },
  { key: "tools", label: "工具调用" },
  // { key: "memory", label: "全局记忆" },
];

const AddAgentModal: React.FC<AddAgentModalProps> = ({
  open,
  onClose,
  createAgentHandle,
  updateAgentHandle,
  editingAgent,
}) => {
  // 菜单项
  const [selectedKey, setSelectedKey] = useState<string>("base");

  // 获取知识库列表
  const { knowledgeBases } = useKnowledgeBases();

  // 工具列表
  const [tools, setTools] = useState<ToolVO[]>([]);

  // 表单数据
  const [formData, setFormData] = useState<CreateAgentRequest>({
    name: "agent",
    description: "",
    systemPrompt: "",
    model: "deepseek-chat",
    allowedTools: [],
    allowedKbs: [],
    chatOptions: {
      temperature: 0.7,
      topP: 1.0,
      messageLength: 10,
    },
  });

  const [createAgentLoading, setCreateAgentLoading] = useState(false);

  // 当编辑的 agent 变化时，更新表单数据
  useEffect(() => {
    if (editingAgent) {
      setFormData({
        name: editingAgent.name,
        description: editingAgent.description || "",
        systemPrompt: editingAgent.systemPrompt || "",
        model: editingAgent.model,
        allowedTools: editingAgent.allowedTools || [],
        allowedKbs: editingAgent.allowedKbs || [],
        chatOptions: editingAgent.chatOptions || {
          temperature: 0.7,
          topP: 1.0,
          messageLength: 10,
        },
      });
    } else {
      // 重置表单
      setFormData({
        name: "agent",
        description: "",
        systemPrompt: "",
        model: "deepseek-chat",
        allowedTools: [],
        allowedKbs: [],
        chatOptions: {
          temperature: 0.7,
          topP: 1.0,
          messageLength: 10,
        },
      });
    }
  }, [editingAgent, open]);

  // 获取工具列表
  useEffect(() => {
    async function fetchTools() {
      try {
        const resp = await getOptionalTools();
        setTools(resp.tools);
      } catch (error) {
        console.error("获取工具列表失败:", error);
      }
    }

    fetchTools().then();
  }, []);

  const isEditMode = !!editingAgent;

  return (
    <Modal
      open={open}
      onCancel={onClose}
      title={isEditMode ? "编辑智能体" : "智能体助手"}
      footer={null}
      width={800}
      centered
    >
      <div className="flex h-[500px]">
        <div className="w-[150px] h-full border-r border-gray-200 pr-2">
          <div className="flex flex-col gap-0.5 select-none cursor-pointer">
            {menuItems.map((item) => {
              const isSelected = selectedKey === item.key;
              return (
                <React.Fragment key={item.key}>
                  <div
                    onClick={() => setSelectedKey(item.key)}
                    className={`px-3 py-2 rounded-lg hover:bg-gray-100 ${isSelected ? "bg-gray-100 text-gray-900 font-medium" : "text-gray-600"}`}
                  >
                    {item.label}
                  </div>
                </React.Fragment>
              );
            })}
          </div>
        </div>
        <div className="flex-1 h-full relative">
          <div className="px-4 pb-4 overflow-y-scroll">
            {selectedKey === "base" && (
              <div>
                <div className="mb-3">
                  <label className="block text-gray-700 font-medium mb-1">
                    名称
                  </label>
                  <div className="flex items-center">
                    <Input
                      placeholder="请输入智能体名称"
                      value={formData.name}
                      onChange={(e) =>
                        setFormData({ ...formData, name: e.target.value })
                      }
                    />
                  </div>
                </div>
                <div className="mb-3">
                  <label className="block text-gray-700 font-medium mb-1">
                    描述
                  </label>
                  <TextArea
                    placeholder="请输入智能体描述"
                    rows={2}
                    value={formData.description}
                    onChange={(e) =>
                      setFormData({ ...formData, description: e.target.value })
                    }
                  />
                </div>
                <div className="mb-3">
                  <label className="block text-gray-700 font-medium mb-1">
                    提示词
                  </label>
                  <TextArea
                    placeholder="默认提示词"
                    rows={11}
                    value={formData.systemPrompt}
                    onChange={(e) =>
                      setFormData({ ...formData, systemPrompt: e.target.value })
                    }
                  />
                </div>
              </div>
            )}
            {selectedKey === "model" && (
              <div>
                <div className="mb-4">
                  <label className="block text-gray-700 font-medium mb-1">
                    选择模型
                  </label>
                  <Select
                    options={[
                      {
                        value: "deepseek-chat",
                        label: "deepseek-chat",
                      },
                      {
                        value: "glm-4.6",
                        label: "glm-4.6",
                      },
                    ]}
                    placeholder="请选择模型"
                    style={{ width: "300px" }}
                    value={formData.model}
                    onChange={(value: ModelType) =>
                      setFormData({ ...formData, model: value })
                    }
                  />
                </div>
                <div className="mb-4">
                  <label className="block text-gray-700 font-medium mb-2">
                    模型参数
                  </label>
                  <div className="space-y-4">
                    <div>
                      <div className="flex items-center justify-between mb-2">
                        <label className="block text-sm text-gray-600">
                          Temperature（温度）
                          <span className="text-gray-400 ml-1 text-xs">
                            (0.0 - 2.0)
                          </span>
                        </label>
                        <span className="text-sm font-medium text-gray-700 min-w-[40px] text-right">
                          {formData?.chatOptions?.temperature?.toFixed(1)}
                        </span>
                      </div>
                      <Slider
                        min={0}
                        max={2}
                        step={0.1}
                        value={formData?.chatOptions?.temperature}
                        onChange={(value) =>
                          setFormData({
                            ...formData,
                            chatOptions: {
                              ...formData.chatOptions,
                              temperature: value,
                            },
                          })
                        }
                      />
                    </div>
                    <div>
                      <div className="flex items-center justify-between mb-2">
                        <label className="block text-sm text-gray-600">
                          Top P（核采样）
                          <span className="text-gray-400 ml-1 text-xs">
                            (0.0 - 1.0)
                          </span>
                        </label>
                        <span className="text-sm font-medium text-gray-700 min-w-[40px] text-right">
                          {formData?.chatOptions?.topP?.toFixed(1)}
                        </span>
                      </div>
                      <Slider
                        min={0}
                        max={1}
                        step={0.1}
                        value={formData?.chatOptions?.topP}
                        onChange={(value) =>
                          setFormData({
                            ...formData,
                            chatOptions: {
                              ...formData.chatOptions,
                              topP: value,
                            },
                          })
                        }
                      />
                    </div>
                    <div>
                      <div className="flex items-center justify-between mb-2">
                        <label className="block text-sm text-gray-600">
                          消息窗口长度
                          <span className="text-gray-400 ml-1 text-xs">
                            (1 - 100)
                          </span>
                        </label>
                        <span className="text-sm font-medium text-gray-700 min-w-[40px] text-right">
                          {formData?.chatOptions?.messageLength}
                        </span>
                      </div>
                      <Slider
                        min={1}
                        max={100}
                        step={1}
                        value={formData?.chatOptions?.messageLength}
                        onChange={(value) =>
                          setFormData({
                            ...formData,
                            chatOptions: {
                              ...formData.chatOptions,
                              messageLength: value,
                            },
                          })
                        }
                      />
                    </div>
                  </div>
                </div>
              </div>
            )}

            {selectedKey === "knowledge" && (
              <div>
                <div className="mb-4">
                  <label className="block text-gray-700 font-medium mb-3">
                    知识库
                  </label>
                  <p className="text-sm text-gray-500 mb-4">
                    选择智能体可以访问的知识库，支持多选（最多10个）
                  </p>
                  {knowledgeBases.length === 0 ? (
                    <div className="text-center py-8 text-gray-500">
                      <p>暂无知识库，请先创建知识库</p>
                    </div>
                  ) : (
                    <div className="space-y-3">
                      {knowledgeBases.map((kb) => {
                        const kbId = kb.knowledgeBaseId;
                        const isSelected = formData.allowedKbs?.includes(kbId);
                        return (
                          <div
                            key={kbId}
                            className={`border rounded-lg p-4 cursor-pointer transition-all hover:border-blue-400 hover:bg-blue-50 ${
                              isSelected
                                ? "border-blue-500 bg-blue-50"
                                : "border-gray-200"
                            }`}
                            onClick={() => {
                              const currentKbs = formData.allowedKbs || [];
                              if (isSelected) {
                                setFormData({
                                  ...formData,
                                  allowedKbs: currentKbs.filter(
                                    (k) => k !== kbId,
                                  ),
                                });
                              } else {
                                if (currentKbs.length >= 10) {
                                  return; // 最多选择10个
                                }
                                setFormData({
                                  ...formData,
                                  allowedKbs: [...currentKbs, kbId],
                                });
                              }
                            }}
                          >
                            <div className="flex items-start gap-2">
                              <Checkbox
                                checked={isSelected}
                                onChange={(e) => {
                                  e.stopPropagation();
                                  const currentKbs = formData.allowedKbs || [];
                                  if (e.target.checked) {
                                    if (currentKbs.length >= 10) {
                                      return; // 最多选择10个
                                    }
                                    setFormData({
                                      ...formData,
                                      allowedKbs: [...currentKbs, kbId],
                                    });
                                  } else {
                                    setFormData({
                                      ...formData,
                                      allowedKbs: currentKbs.filter(
                                        (k) => k !== kbId,
                                      ),
                                    });
                                  }
                                }}
                                className="mr-3"
                              />
                              <div className="flex-1">
                                <div className="flex items-center mb-1">
                                  <span className="font-medium text-gray-900">
                                    {kb.name}
                                  </span>
                                </div>
                                {kb.description && (
                                  <p className="text-sm text-gray-600">
                                    {kb.description}
                                  </p>
                                )}
                              </div>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
                <div>
                  <label className="block text-gray-700 font-medium mb-1">
                    检索设置
                  </label>
                </div>
              </div>
            )}
            {selectedKey === "tools" && (
              <div>
                <div className="mb-4">
                  <label className="block text-gray-700 font-medium mb-3">
                    工具调用
                  </label>
                  <p className="text-sm text-gray-500 mb-4">
                    选择智能体可以使用的工具，支持多选
                  </p>
                  {tools.length === 0 ? (
                    <div className="text-center py-8 text-gray-500">
                      <p>暂无可用工具</p>
                    </div>
                  ) : (
                    <div className="space-y-3">
                      {tools.map((tool) => {
                        const toolId = tool.name;
                        const isSelected =
                          formData.allowedTools?.includes(toolId);
                        return (
                          <div
                            key={toolId}
                            className={`border rounded-lg p-4 cursor-pointer transition-all hover:border-blue-400 hover:bg-blue-50 ${
                              isSelected
                                ? "border-blue-500 bg-blue-50"
                                : "border-gray-200"
                            }`}
                            onClick={() => {
                              const currentTools = formData.allowedTools || [];
                              if (isSelected) {
                                setFormData({
                                  ...formData,
                                  allowedTools: currentTools.filter(
                                    (t) => t !== toolId,
                                  ),
                                });
                              } else {
                                setFormData({
                                  ...formData,
                                  allowedTools: [...currentTools, toolId],
                                });
                              }
                            }}
                          >
                            <div className="flex items-start gap-2">
                              <Checkbox
                                checked={isSelected}
                                onChange={(e) => {
                                  e.stopPropagation();
                                  const currentTools =
                                    formData.allowedTools || [];
                                  if (e.target.checked) {
                                    setFormData({
                                      ...formData,
                                      allowedTools: [...currentTools, toolId],
                                    });
                                  } else {
                                    setFormData({
                                      ...formData,
                                      allowedTools: currentTools.filter(
                                        (t) => t !== toolId,
                                      ),
                                    });
                                  }
                                }}
                                className="mr-3"
                              />
                              <div className="flex-1">
                                <div className="flex items-center mb-1">
                                  <span className="font-medium text-gray-900">
                                    {tool.name}
                                  </span>
                                </div>
                                <p className="text-sm text-gray-600">
                                  {tool.description}
                                </p>
                              </div>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>
          <div className="absolute bottom-0 right-0">
            <Button
              type="primary"
              icon={<SaveOutlined />}
              loading={createAgentLoading}
              onClick={async () => {
                setCreateAgentLoading(true);
                try {
                  if (isEditMode && editingAgent && updateAgentHandle) {
                    await updateAgentHandle(editingAgent.id, formData);
                  } else {
                    await createAgentHandle(formData);
                  }
                  onClose();
                } finally {
                  setCreateAgentLoading(false);
                }
              }}
            >
              {isEditMode ? "更新" : "保存"}
            </Button>
          </div>
        </div>
      </div>
    </Modal>
  );
};

export default AddAgentModal;

package com.kama.jchatmind.model.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * @TableName agent
 */
@Data
@Builder
public class Agent {
    private String id;

    private String name;

    private String description;

    private String systemPrompt;

    private String model;

    // JSON String
    private String allowedTools;

    // JSON String
    private String allowedKbs;

    // JSON String
    private String chatOptions;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Agent other = (Agent) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
                && (this.getDescription() == null ? other.getDescription() == null : this.getDescription().equals(other.getDescription()))
                && (this.getSystemPrompt() == null ? other.getSystemPrompt() == null : this.getSystemPrompt().equals(other.getSystemPrompt()))
                && (this.getModel() == null ? other.getModel() == null : this.getModel().equals(other.getModel()))
                && (this.getAllowedTools() == null ? other.getAllowedTools() == null : this.getAllowedTools().equals(other.getAllowedTools()))
                && (this.getAllowedKbs() == null ? other.getAllowedKbs() == null : this.getAllowedKbs().equals(other.getAllowedKbs()))
                && (this.getChatOptions() == null ? other.getChatOptions() == null : this.getChatOptions().equals(other.getChatOptions()))
                && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
                && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
        result = prime * result + ((getSystemPrompt() == null) ? 0 : getSystemPrompt().hashCode());
        result = prime * result + ((getModel() == null) ? 0 : getModel().hashCode());
        result = prime * result + ((getAllowedTools() == null) ? 0 : getAllowedTools().hashCode());
        result = prime * result + ((getAllowedKbs() == null) ? 0 : getAllowedKbs().hashCode());
        result = prime * result + ((getChatOptions() == null) ? 0 : getChatOptions().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                " [" +
                "Hash = " + hashCode() +
                ", id=" + id +
                ", name=" + name +
                ", description=" + description +
                ", systemPrompt=" + systemPrompt +
                ", model=" + model +
                ", allowedTools=" + allowedTools +
                ", allowedKbs=" + allowedKbs +
                ", chatOptions=" + chatOptions +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                "]";
    }
}
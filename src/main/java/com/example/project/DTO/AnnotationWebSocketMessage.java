package com.example.project.DTO;

import com.example.project.entities.AnnotationType;

public class AnnotationWebSocketMessage {
    
    public enum Action {
        CREATE, UPDATE, DELETE, USER_JOINED, USER_LEFT, CURSOR_MOVE
    }

    private Action action;
    private Integer versionId;
    private Integer pageNumber;
    private AnnotationDTO annotation;
    private UserSummaryDTO user;
    private String coordinates; 
    private String sessionId;
    private Long timestamp;

    
    public AnnotationWebSocketMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public AnnotationWebSocketMessage(Action action, Integer versionId, AnnotationDTO annotation, UserSummaryDTO user) {
        this();
        this.action = action;
        this.versionId = versionId;
        this.annotation = annotation;
        this.user = user;
        if (annotation != null) {
            this.pageNumber = annotation.getPageNumber();
        }
    }

    
    public static AnnotationWebSocketMessage createAnnotation(Integer versionId, AnnotationDTO annotation, UserSummaryDTO user) {
        return new AnnotationWebSocketMessage(Action.CREATE, versionId, annotation, user);
    }

    public static AnnotationWebSocketMessage updateAnnotation(Integer versionId, AnnotationDTO annotation, UserSummaryDTO user) {
        return new AnnotationWebSocketMessage(Action.UPDATE, versionId, annotation, user);
    }

    public static AnnotationWebSocketMessage deleteAnnotation(Integer versionId, AnnotationDTO annotation, UserSummaryDTO user) {
        return new AnnotationWebSocketMessage(Action.DELETE, versionId, annotation, user);
    }

    public static AnnotationWebSocketMessage userJoined(Integer versionId, UserSummaryDTO user) {
        return new AnnotationWebSocketMessage(Action.USER_JOINED, versionId, null, user);
    }

    public static AnnotationWebSocketMessage userLeft(Integer versionId, UserSummaryDTO user) {
        return new AnnotationWebSocketMessage(Action.USER_LEFT, versionId, null, user);
    }

    public static AnnotationWebSocketMessage cursorMove(Integer versionId, Integer pageNumber, String coordinates, UserSummaryDTO user) {
        AnnotationWebSocketMessage message = new AnnotationWebSocketMessage();
        message.action = Action.CURSOR_MOVE;
        message.versionId = versionId;
        message.pageNumber = pageNumber;
        message.coordinates = coordinates;
        message.user = user;
        return message;
    }

    
    public Action getAction() { return action; }
    public void setAction(Action action) { this.action = action; }

    public Integer getVersionId() { return versionId; }
    public void setVersionId(Integer versionId) { this.versionId = versionId; }

    public Integer getPageNumber() { return pageNumber; }
    public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }

    public AnnotationDTO getAnnotation() { return annotation; }
    public void setAnnotation(AnnotationDTO annotation) { this.annotation = annotation; }

    public UserSummaryDTO getUser() { return user; }
    public void setUser(UserSummaryDTO user) { this.user = user; }

    public String getCoordinates() { return coordinates; }
    public void setCoordinates(String coordinates) { this.coordinates = coordinates; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}

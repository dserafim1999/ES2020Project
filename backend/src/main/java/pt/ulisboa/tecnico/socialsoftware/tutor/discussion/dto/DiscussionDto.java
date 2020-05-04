package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Reply;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;

public class DiscussionDto implements Serializable {
    private Integer userId;
    private String userName;
    private QuestionDto question;
    private String content;
    private List<ReplyDto> replies;
    private String date;

    public DiscussionDto() {
    }

	public DiscussionDto(Discussion discussion) {
        this.userId = discussion.getId().getUserId();
        this.userName = discussion.getUser().getName();
        this.content = discussion.getContent();
        this.question = new QuestionDto(discussion.getQuestion());
        this.date = DateHandler.toISOString(discussion.getDate());

        List<Reply> discussionReplies = discussion.getReplies();
        if(discussionReplies != null && !discussionReplies.isEmpty()) {
            this.replies = new ArrayList<>();
            for (Reply r : discussionReplies) {
                this.replies.add(new ReplyDto(r));
            }
        }
    }

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

    public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

    public Integer getUserId() {
        return this.userId;
    }

    public Integer getQuestionId() {
        return this.question.getId();
    }

    public void setUserId(Integer id) {
        this.userId = id;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public QuestionDto getQuestion() {
        return this.question;
    }

    public void setQuestion(QuestionDto question) {
        this.question = question;
    }

    public List<ReplyDto> getReplies() {
        return this.replies;
    }

    public void addReply(ReplyDto reply) {
        this.replies.add(reply);
    }
}

package pl.lipinski.engineerdegree.dao.dto;


public class StringResponseDto {
    private String responseContent;

    public StringResponseDto() {
    }

    public StringResponseDto(String responseContent) {
        this.responseContent = responseContent;
    }

    public String getResponseContent() {
        return responseContent;
    }

    public void setResponseContent(String responseContent) {
        this.responseContent = responseContent;
    }
}

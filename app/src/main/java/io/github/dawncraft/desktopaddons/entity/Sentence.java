package io.github.dawncraft.desktopaddons.entity;

/**
 * 用于展示的句子信息
 *
 * @author QingChenW
 */
public class Sentence
{
    private int id;
    private String uuid;
    private String sentence;
    private String author;
    private String from;
    private Source source;
    private String committer;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getUUID()
    {
        return uuid;
    }

    public void setUUID(String uuid)
    {
        this.uuid = uuid;
    }

    public String getSentence()
    {
        return sentence;
    }

    public void setSentence(String sentence)
    {
        this.sentence = sentence;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public String getFrom()
    {
        return from;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    public Source getSource()
    {
        return source;
    }

    public void setSource(Source source)
    {
        this.source = source;
    }

    public String getCommitter()
    {
        return committer;
    }

    public void setCommitter(String committer)
    {
        this.committer = committer;
    }

    public enum Source
    {
        Hitokoto,  // 一言
        Dawncraft; // 我的API
    }
}

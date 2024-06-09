package co.kr.necohost.semi.domain.service;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class DiscordBotService {

    @Value("${discord.bot.token}")
    private String botToken;

    @Value("${discord.channel.id}")
    private String channelId;

    private JDA jda;

    @PostConstruct
    public void init() {
        try {
            jda = JDABuilder.createDefault(botToken).build();
            jda.awaitReady(); // JDA가 완전히 초기화될 때까지 대기
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendOrderNotification(long orderId) {
        String message = "주문 번호 " + orderId + "가 승인되었습니다.";
        MessageChannel channel = jda.getChannelById(MessageChannel.class, channelId);
        if (channel != null) {
            channel.sendMessage(message).queue();
        } else {
            System.out.println("채널을 찾을 수 없습니다: " + channelId);
        }
    }
}

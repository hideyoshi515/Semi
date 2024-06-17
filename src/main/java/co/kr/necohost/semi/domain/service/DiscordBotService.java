package co.kr.necohost.semi.domain.service;

import co.kr.necohost.semi.domain.model.entity.Sales;
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

	// Discord Botを初期化
	@PostConstruct
	public void init() {
		try {
			jda = JDABuilder.createDefault(botToken).build();
			jda.awaitReady();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 注文通知を送信
	public void sendOrderNotification(String message) {
		MessageChannel channel = jda.getChannelById(MessageChannel.class, channelId);

		if (channel != null) {
			channel.sendMessage("```" + message + "```").queue();
		} else {
			System.out.println("채널을 찾을 수 없습니다: " + channelId);
		}
	}
}

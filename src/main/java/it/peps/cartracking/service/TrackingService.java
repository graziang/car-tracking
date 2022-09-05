package it.peps.cartracking.service;

import it.peps.cartracking.bot.TrackingBot;
import it.peps.cartracking.model.TrackingData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
@Service
public class TrackingService {

    private final String HOST = "https://api.whereismymme.com";
    private final String VAR_VIN ="${vin}";
    private final String VAR_JOB_ID ="${jobid}";
    private final String PATH_SEARCH = HOST + "/search?vin=" + VAR_VIN;
    private final String PATH_TRACK = HOST + "/search/searchjob/"+ VAR_JOB_ID + "/" + VAR_VIN;

    public Map<String, TrackingData.Order> vimStatus = new HashMap<>();


    @Autowired
    TrackingBot trackingBot;


    @Async
    @Scheduled(fixedDelay = 60 * 60 * 1000)
    public void initClient(){

        for (String chatId: trackingBot.getIds().keySet()) {

            String vim = trackingBot.getIds().get(chatId);
            try {
                RestTemplate restTemplate = new RestTemplate();
                String url = PATH_SEARCH.replace(VAR_VIN, vim);
                Map<String, String> res = restTemplate.getForObject(url, Map.class);
                String id = res.get("id");

                url = PATH_TRACK.replace(VAR_JOB_ID, id);
                url = url.replace(VAR_VIN, vim);

                TrackingData data = restTemplate.getForObject(url, TrackingData.class);
                int i = 0;
                while (data.getReturnvalue() == null && i < 15) {
                    data = restTemplate.getForObject(url, TrackingData.class);
                    Thread.sleep(5000);
                    i++;
                }
                log.info(data.toString());
                if(data.getReturnvalue() != null) {


                    TrackingData.Order order = data.getReturnvalue().getResult().getOrder();
                    if (!vimStatus.containsKey(vim) || !vimStatus.get(vim).toString().equals(order.toString())) {
                        vimStatus.put(vim, order);

                        SendMessage response = new SendMessage();
                        response.setChatId(chatId);
                        String text = "STATUS:" + order.getVmacsStatusDesc() + ": DATE:" + order.getVmacsStatusDate();
                        response.setText(text);
                        trackingBot.execute(response);

                        response = new SendMessage();
                        response.setChatId(chatId);
                        text = data.getReturnvalue().getResult().toString();
                        response.setText(text);
                        trackingBot.execute(response);


                    }

                }


            } catch (Exception e) {
                log.error("Error getting status", e);
                SendMessage response = new SendMessage();
                response.setChatId(chatId);
                response.setText("Error sending message for chat: " + chatId);
                try {
                    trackingBot.execute(response);
                } catch (TelegramApiException ex) {
                    log.error("Telegram error", ex);
                }
            }
        }
    }
}

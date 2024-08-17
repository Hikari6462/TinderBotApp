package com.codegym.telegram;

import com.plexpt.chatgpt.ChatGPT;
import com.plexpt.chatgpt.entity.chat.ChatCompletion;
import com.plexpt.chatgpt.entity.chat.ChatCompletionResponse;
import com.plexpt.chatgpt.entity.chat.Message;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatGPTService {
    private ChatGPT chatGPT;

    private List<Message> messageHistory = new ArrayList<>(); //Historia de mensajes con ChatGPT; necesaria para los diálogos

    public ChatGPTService(String token) {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("18.199.183.77", 49232));
        if (token.startsWith("gpt:")) {
            token = "sk-proj-" + new StringBuilder(token.substring(4)).reverse();
        }

        this.chatGPT = ChatGPT.builder()
                .apiKey(token)
                .apiHost("https://api.openai.com/")
                .proxy(proxy)
                .build()
                .init();
    }

    /**
     * Consulta individual a ChatGPT en el formato "consulta"-> "respuesta".
     * La consulta consta de dos partes:
     * prompt - contexto de la pregunta
     * question - la consulta en sí
     */
    public String sendMessage(String prompt, String question) {
        Message system = Message.ofSystem(prompt);
        Message message = Message.of(question);
        messageHistory = new ArrayList<>(Arrays.asList(system, message));

        return sendMessagesToChatGPT();
    }

    /**
     * Consultas a ChatGPT con historial de mensajes guardado.
     * El método setPrompt() establece el contexto de la consulta
     */
    public void setPrompt(String prompt) {
        Message system = Message.ofSystem(prompt);
        messageHistory = new ArrayList<>(List.of(system));
    }

    /**
     * Consultas a ChatGPT con historial de mensajes guardado.
     * El método addMessage() añade una nueva pregunta (mensaje) al chat.
     */
    public String addMessage(String question) {
        Message message = Message.of(question);
        messageHistory.add(message);

        return sendMessagesToChatGPT();
    }

    /**
     * Enviamos a ChatGPT una serie de mensajes: prompt, message1, answer1, message2, answer2, ..., messageN
     * La respuesta de ChatGPT se añade al final de messageHistory para su uso posterior
     */
    private String sendMessagesToChatGPT() {
        ChatCompletion chatCompletion = ChatCompletion.builder()
                .model("gpt-3.5-turbo") //  gpt-4o,  gpt-4-turbo,    gpt-3.5-turbo
                .messages(messageHistory)
                .maxTokens(3000)
                .temperature(0.9)
                .build();

        ChatCompletionResponse response = chatGPT.chatCompletion(chatCompletion);
        Message res = response.getChoices().get(0).getMessage();
        messageHistory.add(res);

        return res.getContent();
    }
}

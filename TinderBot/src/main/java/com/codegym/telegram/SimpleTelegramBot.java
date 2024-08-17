package com.codegym.telegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.api.methods.GetUserProfilePhotos;
import org.telegram.telegrambots.meta.api.methods.commands.DeleteMyCommands;
import org.telegram.telegrambots.meta.api.methods.commands.GetMyCommands;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.menubutton.SetChatMenuButton;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat;
import org.telegram.telegrambots.meta.api.objects.menubutton.MenuButtonCommands;
import org.telegram.telegrambots.meta.api.objects.menubutton.MenuButtonDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SimpleTelegramBot extends TelegramLongPollingBot {

    private String token;
    private volatile boolean isInitialized = false;
    private List<MyFunctionalInterface> handlerList = new ArrayList<>();
    private ThreadLocal<Update> updateEvent = new ThreadLocal<>();

    public SimpleTelegramBot(String token) {
        this.token = token;
    }

    @Override
    public String getBotUsername() {
        return "javarush_marathon";
    }

    @Override
    public String getBotToken() {
        return token;
    }


    @Override
    public final void onUpdateReceived(Update updateEvent) {
        //call onInitialize() with try..catch
        try {
            if (!isInitialized) {
                isInitialized = true;
                onInitialize();
            }
        } catch (Exception e) {
            System.out.println("onInitialize ERROR: " + e.getMessage());
            throw new RuntimeException(e);
        }

        //call handler-list with try..catch for every handler 
        try {
            this.updateEvent.set(updateEvent);

            for (var handler : handlerList) {
                try {
                    handler.execute();
                } catch (Exception e) {
                    System.out.println("onHandler ERROR: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            onUpdateEventReceived(this.updateEvent.get());
        } catch (Exception e) {
            System.out.println("onUpdateEventReceived ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Necesita redefinir este método para inicializar el bot
     */
    public void onInitialize() {
        // do nothing
    }

    /**
     * Agregar command-handler al bot
     */
    public void addCommandHandler(String command, MyFunctionalInterface method) {
        handlerList.add(() -> {
            String messageText = getMessageText();
            if (("/" + command).equals(messageText))
                method.execute();
        });
    }

    /**
     * Agregar button-handler al bot
     */
    public void addButtonHandler(String regex, MyFunctionalInterface method) {
        handlerList.add(() -> {
            String buttonKey = getButtonKey();
            if (Pattern.matches(regex, buttonKey) && !buttonKey.isEmpty())
                method.execute();
        });
    }

    /**
     * Agregar message-handler al bot
     */
    public void addMessageHandler(MyFunctionalInterface method) {
        handlerList.add(() -> {
            String messageText = getMessageText();
            if (!isMessageCommand() && !messageText.isEmpty())
                method.execute();
        });
    }

    public void onUpdateEventReceived(Update updateEvent) throws Exception {
        //do nothing
    }

    /**
     * El método devuelve el ID del chat de Telegram actual
     */
    public Long getCurrentChatId() {
        if (updateEvent.get().hasMessage()) {
            return updateEvent.get().getMessage().getFrom().getId();
        }

        if (updateEvent.get().hasCallbackQuery()) {
            return updateEvent.get().getCallbackQuery().getFrom().getId();
        }

        return null;
    }

    /**
     * El método devuelve el texto del último mensaje del chat de Telegram
     */
    public String getMessageText() {
        return updateEvent.get().hasMessage() ? updateEvent.get().getMessage().getText() : "";
    }

    public boolean isMessageCommand() {
        return updateEvent.get().hasMessage() && updateEvent.get().getMessage().isCommand();
    }

    /**
     El método devuelve el código del botón presionado (buttonKey).
     Se refiere a los botones que se añadieron al mensaje.
     */
    public String getButtonKey() {
        return updateEvent.get().hasCallbackQuery() ? updateEvent.get().getCallbackQuery().getData() : "";
    }

    /**
     El método envía TEXTO (mensaje de texto) al chat.
     Se admite la sintaxis de markdown.
     */
    public Message sendTextMessage(String text) {
        SendMessage command = createApiSendMessageCommand(text);
        return executeTelegramApiMethod(command);
    }

    /**
     El método envía HTML (mensaje de texto) al chat.
     Se admite la sintaxis de html.
     */
    public Message sendHtmlMessage(String text) {
        SendMessage message = new SendMessage();
        message.setText(new String(text.getBytes(), StandardCharsets.UTF_8));
        message.setParseMode("html");
        message.setChatId(getCurrentChatId());
        return executeTelegramApiMethod(message);
    }

    /**
     El método envía una FOTO (Imagen) al chat.
     La imagen se especifica con la clave – photoKey.
     Todas las imágenes se encuentran en la carpeta resources/images
     */
    public Message sendPhotoMessage(String photoKey) {
        SendPhoto command = createApiPhotoMessageCommand(photoKey, null);
        return executeTelegramApiMethod(command);
    }

    /**
     El método envía una FOTO (Imagen) y TEXTO al chat.
     La imagen se especifica con la clave – photoKey.
     Todas las imágenes se encuentran en la carpeta resources/images
     */
    public Message sendPhotoTextMessage(String photoKey, String text) {
        SendPhoto command = createApiPhotoMessageCommand(photoKey, text);
        return executeTelegramApiMethod(command);
    }

    /**
     * El método cambia el TEXTO en un mensaje enviado previamente.
     */
    public void updateTextMessage(Message message, String text) {
        EditMessageText command = new EditMessageText();
        command.setChatId(message.getChatId());
        command.setMessageId(message.getMessageId());
        command.setText(text);
        executeTelegramApiMethod(command);
    }

    /**
     Mensaje con botones (Inline Buttons)
     */
    public Message sendTextButtonsMessage(String text, String... buttons) {
        SendMessage command = createApiSendMessageCommand(text);
        if (buttons.length > 0)
            attachButtons(command, List.of(buttons));

        return executeTelegramApiMethod(command);
    }

    /**
     Mensaje con botones (Inline Buttons)
     */
    public void sendTextButtonsMessage(String text, List<String> buttons) {
        SendMessage command = createApiSendMessageCommand(text);
        if (buttons != null && !buttons.isEmpty())
            attachButtons(command, buttons);

        executeTelegramApiMethod(command);
    }

    public void showMainMenu(String... commands) {
        ArrayList<BotCommand> list = new ArrayList<BotCommand>();

        //convert strings to command list
        for (int i = 0; i < commands.length; i += 2) {
            String key = commands[i];
            String description = commands[i + 1];

            if (key.startsWith("/")) //remove first /
                key = key.substring(1);

            BotCommand bc = new BotCommand(key, description);
            list.add(bc);
        }

        //obtener la lista de comandos
        var chatId = getCurrentChatId();
        GetMyCommands gmcs = new GetMyCommands();
        gmcs.setScope(BotCommandScopeChat.builder().chatId(chatId).build());
        ArrayList<BotCommand> oldCommands = executeTelegramApiMethod(gmcs);

        //ignorar cambios de comandos para la misma lista de comandos
        if (oldCommands.equals(list))
            return;

        //establecer la lista de comandos
        SetMyCommands cmds = new SetMyCommands();
        cmds.setCommands(list);
        cmds.setScope(BotCommandScopeChat.builder().chatId(chatId).build());
        executeTelegramApiMethod(cmds);

        //mostrar el botón del menú
        var ex = new SetChatMenuButton();
        ex.setChatId(chatId);
        ex.setMenuButton(MenuButtonCommands.builder().build());
        executeTelegramApiMethod(ex);
    }

    public void hideMainMenu() {
        //eliminar la lista de comandos
        var chatId = getCurrentChatId();
        DeleteMyCommands dmds = new DeleteMyCommands();
        dmds.setScope(BotCommandScopeChat.builder().chatId(chatId).build());
        executeTelegramApiMethod(dmds);

        //ocultar el botón del menú
        var ex = new SetChatMenuButton();
        ex.setChatId(chatId);
        ex.setMenuButton(MenuButtonDefault.builder().build());
        executeTelegramApiMethod(ex);
    }

    public List<List<PhotoSize>> getUserProfilePhotos() {
        var userId = getCurrentChatId();
        var request = GetUserProfilePhotos.builder().userId(userId).offset(0).limit(100).build();
        UserProfilePhotos userProfilePhotos = executeTelegramApiMethod(request);
        return userProfilePhotos.getPhotos();
    }

    public List<List<PhotoSize>> getChatBotProfilePhotos() {
        var me = executeTelegramApiMethod(new GetMe());
        var userId = me.getId();
        var request = GetUserProfilePhotos.builder().userId(userId).offset(0).limit(100).build();
        UserProfilePhotos userProfilePhotos = executeTelegramApiMethod(request);
        return userProfilePhotos.getPhotos();
    }

    private SendMessage createApiSendMessageCommand(String text) {
        SendMessage message = new SendMessage();
        message.setText(new String(text.getBytes(), StandardCharsets.UTF_8));
        message.setParseMode("markdown");
        message.setChatId(getCurrentChatId());
        return message;
    }

    private void attachButtons(SendMessage message, List<String> buttons) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (int i = 0; i < buttons.size(); i += 2) {
            String buttonKey = buttons.get(i);
            String buttonName = buttons.get(i + 1);

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(new String(buttonName.getBytes(), StandardCharsets.UTF_8));
            button.setCallbackData(buttonKey);

            keyboard.add(List.of(button));
        }

        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);
    }

    private SendPhoto createApiPhotoMessageCommand(String photoKey, String text) {
        try {
            InputFile inputFile = new InputFile();
            var is = loadImage(photoKey);
            inputFile.setMedia(is, photoKey);

            SendPhoto photo = new SendPhoto();
            photo.setPhoto(inputFile);
            photo.setChatId(getCurrentChatId());

            if (text != null && !text.isEmpty())
                photo.setCaption(text);

            return photo;
        } catch (Exception e) {
            throw new RuntimeException("Can't create photo message!");
        }
    }


    public static String loadPrompt(String name) {
        try {
            var is = ClassLoader.getSystemResourceAsStream("prompts/" + name + ".txt");
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Can't load GPT prompt!");
        }
    }

    public static String loadMessage(String name) {
        try {
            var is = ClassLoader.getSystemResourceAsStream("messages/" + name + ".txt");
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Can't load message!");
        }
    }

    public static InputStream loadImage(String name) {
        try {
            return ClassLoader.getSystemResourceAsStream("images/" + name + ".jpg");
        } catch (Exception e) {
            throw new RuntimeException("Can't load photo!");
        }
    }


    private <T extends Serializable, Method extends BotApiMethod<T>> T executeTelegramApiMethod(Method method) {
        try {
            return super.sendApiMethod(method);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private Message executeTelegramApiMethod(SendPhoto message) {
        try {
            return super.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public static interface MyFunctionalInterface {
        void execute();
    }
}

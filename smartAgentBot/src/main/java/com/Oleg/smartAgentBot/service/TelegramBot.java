package com.Oleg.smartAgentBot.service;

import com.Oleg.smartAgentBot.config.BotConfig;
import com.Oleg.smartAgentBot.model.Apartments;
import com.Oleg.smartAgentBot.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    @Autowired
    private UserService userService;


    @Autowired
    private ApartmetsService apartmentsService;

    private Map<Long, Apartments> apartmentsMap = new HashMap<>();
    private Map<Long, Integer> stepMap = new HashMap<>();

    public TelegramBot(BotConfig config) {
        this.config = config;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    private String selectedDistrict;
    private Integer minPrice;
    private Integer maxPrice;
    private String currentFilterState; // Состояние текущего фильтра
    private List<Apartments> userApartments;
    private boolean flag = false;


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            User user = new User();
            user.setId(chatId);
            user.setUsername(update.getMessage().getFrom().getUserName());

            if (messageText.equals("Добавить ➕")) {
                flag = false;

                startAddingApartment(chatId);
            } else if (apartmentsMap.containsKey(chatId)) {
                flag = false;
                processApartmentInput(chatId, messageText, user);
            } else {
                switch (messageText) {
                    case "/start":
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(chatId);
                        sendMessage.setText("Привет! \uD83D\uDE0A Добро пожаловать в SmartAgentBot! \uD83C\uDFE0\n" +
                                "Я здесь, чтобы помочь вам с поиском и управлением недвижимостью. \uD83D\uDD0E\n" +
                                "Выберите одну из опций ниже, чтобы начать: \uD83D\uDC47");
                        executeMessage(sendMessage);
                        sendLoginAndRegisterButton(chatId);
                        break;
                    case "Личный кабинет \uD83D\uDD14":
                        sendInfoAboutUser(chatId, user);
                        break;
                    case "Помощь \uD83C\uDD98":
                        sendHelpInfo(chatId);
                        break;
                    case "Назад ◀️\uFE0F":
                        sendLoginAndRegisterButton(chatId);
                        break;
                    case "Поиск \uD83D\uDD0D":
                        openKeyBoardTofindApartaments(chatId);
                        break;
                    case "Глобальный поиск":
                        showAllApartament(chatId);
                        break;
                    case "Следующий":
                        nextApartment(chatId);
                        break;
                    case "Предыдущий":
                        previousApartment(chatId);
                        break;
                    case "Фильтр":
                        openFilterButton(chatId, messageText);
                        break;
                    case "Район":
                        flag = true;
                        showDistrictButtons(chatId);
                        break;
                    case "Цена от":
                        currentFilterState = "Цена от";
                        sendMessage(chatId, "Введите минимальную цену:");
                        break;
                    case "Цена до":
                        currentFilterState = "Цена до";
                        sendMessage(chatId, "Введите максимальную цену:");
                        break;
                    case "Назад":
                        openFilterButton(chatId, messageText);
                        break;
                    case "Искать":
                        searchApartments(chatId);
                        break;
                    case "Сброс":
                        deleteFilter(chatId);
                        break;
                    case "Мои объявления":
                        showMyApartaments(chatId);
                        break;
                    case "Назад к л/к":
                        sendInfoAboutUser(chatId, user);
                        break;
                    case "Удалить":
                        deleteApartment(chatId, currentApartmentIndex);
                        break;
                    case "Далее":
                        showNextApartment(chatId);
                        break;
                    case "Обратно":
                        showPreviousApartment(chatId);
                        break;
                    default:
                        handleFilterInput(chatId, messageText);
                        break;
                }
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            if(!flag) {
                handleDistrictSelection(chatId, callbackData);
            }
            else if(flag){
                handleDistrictSelection1(chatId, callbackData);
            }
        }
    }
    private void handleDistrictSelection1(long chatId, String district) {
        selectedDistrict = district;

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Район установлен - " + district);
        executeMessage(sendMessage);
    }


    private void showDistrictButtons(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        // Создаем инлайн-клавиатуру
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // Добавляем кнопки районов
        addDistrictButton(rowsInline, "Юбилейный", "Юбилейный");
        addDistrictButton(rowsInline, "Черёмушки / Дубинка", "Черёмушки / Дубинка");
        addDistrictButton(rowsInline, "Центр", "Центр");
        addDistrictButton(rowsInline, "Энка / Авиагородок 🛫/ Репина", "Энка / Авиагородок 🛫/ Репина");
        addDistrictButton(rowsInline, "Молодежный / Витамин", "Молодежный / Витамин");
        addDistrictButton(rowsInline, "Западный обход / Немецкая деревня", "Западный обход / Немецкая деревня");
        addDistrictButton(rowsInline, "ЗИП / ККБ", "ЗИП / ККБ");
        addDistrictButton(rowsInline, "Российский / Краснодарский", "Российский / Краснодарский");
        addDistrictButton(rowsInline, "РМЗ / ХБК / КСК", "РМЗ / ХБК / КСК");
        addDistrictButton(rowsInline, "Славянский / Рубероидный", "Славянский / Рубероидный");
        addDistrictButton(rowsInline, "Горхутор / Северный", "Горхутор / Северный");
        addDistrictButton(rowsInline, "Прикубанский округ", "Прикубанский округ");
        addDistrictButton(rowsInline, "Ростовское Шоссе", "Ростовское Шоссе");
        addDistrictButton(rowsInline, "Знаменский / Новознаменский", "Знаменский / Новознаменский");
        addDistrictButton(rowsInline, "Гидрострой", "Гидрострой");
        addDistrictButton(rowsInline, "РИП / ЗИП / Московский", "РИП / ЗИП / Московский");
        addDistrictButton(rowsInline, "Яблоновский / Адыгея", "Яблоновский / Адыгея");
        addDistrictButton(rowsInline, "Комсомольский / Пашковский", "Комсомольский / Пашковский");
        addDistrictButton(rowsInline, "Елизаветинская", "Елизаветинская");
        addDistrictButton(rowsInline, "Фестивальный / Аврора", "Фестивальный / Аврора");
        addDistrictButton(rowsInline, "Аренда", "Аренда");
        addDistrictButton(rowsInline, "Запросы", "Запросы");
        addDistrictButton(rowsInline, "Коммерция Краснодар и край", "Коммерция Краснодар и край");
        addDistrictButton(rowsInline, "40 лет / Восточка", "40 лет / Восточка");
        addDistrictButton(rowsInline, "Центр / Западный округ", "Центр / Западный округ");
        addDistrictButton(rowsInline, "Карасунский округ", "Карасунский округ");
        addDistrictButton(rowsInline, "Ейское шоссе / Южный", "Ейское шоссе / Южный");
        addDistrictButton(rowsInline, "х.Ленина / Старокорсунская", "х.Ленина / Старокорсунская");
        addDistrictButton(rowsInline, "Динской район", "Динской район");

        inlineKeyboardMarkup.setKeyboard(rowsInline);
        message.setReplyMarkup(inlineKeyboardMarkup);

        // Отправляем сообщение пользователю
        message.setText("Пожалуйста, выберите район:");

        try {
            execute(message); // Используем метод execute для отправки сообщения
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения: ", e);
        }
    }



    private void addDistrictButton(List<List<InlineKeyboardButton>> rowsInline, String district, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(district);
        button.setCallbackData(callbackData);

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(button);
        rowsInline.add(rowInline);
    }
    private void showMyApartaments(long chatId) {
        userApartments = apartmentsService.findApartmentsByUserId(chatId);

        if (userApartments.isEmpty()) {
            sendMessage(chatId, "У вас нет объявлений.");
            return;
        }

        // Показываем первое объявление и добавляем кнопки для навигации и удаления
        showApartmentWithButtons(chatId, 0);
    }

    private void showApartmentWithButtons(long chatId, int index) {
        if (index < 0 || index >= userApartments.size()) {
            sendMessage(chatId, "Нет больше объявлений.");
            return;
        }

        Apartments currentApartment = userApartments.get(index);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(currentApartment.toString());

        // Создаем клавиатуру с кнопками
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();

        row1.add("Удалить");
        row2.add("Далее");
        row2.add("Обратно");
        row3.add("Назад к л/к");

        keyboardRows.add(row1);
        keyboardRows.add(row2);
        keyboardRows.add(row3);

        keyboardMarkup.setKeyboard(keyboardRows);
        sendMessage.setReplyMarkup(keyboardMarkup);

        executeMessage(sendMessage);
    }

    private void deleteApartment(long chatId, int index) {
        if (index >= 0 && index < userApartments.size()) {
            Apartments apartmentToDelete = userApartments.get(index);
            apartmentsService.deleteApartment(apartmentToDelete.getId());
            userApartments.remove(index);
            sendMessage(chatId, "Объявление удалено.");
            currentApartmentIndex = Math.min(currentApartmentIndex, userApartments.size() - 1);
            showApartmentWithButtons(chatId, currentApartmentIndex);
        } else {
            sendMessage(chatId, "Невозможно удалить объявление.");
        }
    }

    private void showNextApartment(long chatId) {
        if (currentApartmentIndex < userApartments.size() - 1) {
            currentApartmentIndex++;
        } else {
            currentApartmentIndex = 0;
        }
        showApartmentWithButtons(chatId, currentApartmentIndex);
    }

    private void showPreviousApartment(long chatId) {
        if (currentApartmentIndex > 0) {
            currentApartmentIndex--;
        } else {
            currentApartmentIndex = userApartments.size() - 1;
        }
        showApartmentWithButtons(chatId, currentApartmentIndex);
    }




    private void handleFilterInput(long chatId, String input) {
        if (currentFilterState != null) {
            switch (currentFilterState) {
                case "Цена от":
                    handleFilterMinPrice(chatId, input);
                    break;
                case "Цена до":
                    handleFilterMaxPrice(chatId, input);
                    break;
            }
            currentFilterState = null; // Сброс состояния после обработки ввода
        }
    }



    private void handleFilterMinPrice(long chatId, String input) {
        try {
            minPrice = Integer.parseInt(input);
            sendMessage(chatId, "Минимальная цена установлена: " + minPrice);
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Пожалуйста, введите число для минимальной цены.");
        }
    }

    private void handleFilterMaxPrice(long chatId, String input) {
        try {
            int parsedMaxPrice = Integer.parseInt(input);
            if (minPrice != null && parsedMaxPrice < minPrice) {
                sendMessage(chatId, "Максимальная цена не может быть меньше минимальной цены: " + minPrice);
            } else {
                maxPrice = parsedMaxPrice;
                sendMessage(chatId, "Максимальная цена установлена: " + maxPrice);
            }
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Пожалуйста, введите число для максимальной цены.");
        }
    }




    private void openFilterButton(long chatId , String messageText) {
        if (messageText.equals("Назад")) {
            apartmentsMap.remove(chatId);
            stepMap.remove(chatId);
            sendLoginAndRegisterButton(chatId);
            return;
        }

        // Создаем сообщение
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Укажите фильтрацию");

        // Создаем клавиатуру с кнопками
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        // Создаем строки клавиатуры
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        KeyboardRow row4 = new KeyboardRow();

        // Добавляем кнопки в строки
        row1.add("Район");
        row2.add("Цена от");
        row2.add("Цена до");
        row3.add("Назад");
        row3.add("Искать");
        row4.add("Сброс");

        // Добавляем строки в клавиатуру
        keyboardRows.add(row1);
        keyboardRows.add(row2);
        keyboardRows.add(row3);
        keyboardRows.add(row4);

        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);

        // Отправляем сообщение
        executeMessage(message);
    }

    private int currentIndex = 0; // Переменная для отслеживания текущей позиции в списке квартир
    private final int batchSize = 5; // Количество квартир, выводимых за одно нажатие

    private void searchApartments(long chatId) {
        List<Apartments> filteredApartments = apartmentsService.getFilteredApartments(selectedDistrict, minPrice, maxPrice);

        if (filteredApartments.isEmpty()) {
            sendMessage(chatId, "Нет квартир, соответствующих выбранным фильтрам.");
        } else {
            int endIndex = Math.min(currentIndex + batchSize, filteredApartments.size());

            // Проверка, что fromIndex не превышает toIndex
            if (currentIndex <= endIndex) {
                List<Apartments> batch = filteredApartments.subList(currentIndex, endIndex);

                for (Apartments apartment : batch) {
                    sendMessage(chatId, apartment.toString());
                }

                currentIndex = (currentIndex + batchSize) % filteredApartments.size();
            } else {
                // Обработка ошибки или сброс индекса
                currentIndex = 0;
            }
        }
    }
    private void deleteFilter(long chatId) {
        SendMessage sendMessage = new SendMessage();
        selectedDistrict = null;
        minPrice = null;
        maxPrice = null;
        sendMessage.setChatId(chatId);
        sendMessage.setText("Фильр сброшен");
    }

    private int currentApartmentIndex = 0;

    private void showAllApartament(long chatId) {
        List<Apartments> apartments = apartmentsService.findAllApartments();

        if (apartments.isEmpty()) {
            sendMessage(chatId, "Нет доступных апартаментов.");
            return;
        }

        // Обеспечиваем циклический переход
        if (currentApartmentIndex >= apartments.size()) {
            currentApartmentIndex = 0; // Переход к первому элементу
        } else if (currentApartmentIndex < 0) {
            currentApartmentIndex = apartments.size() - 1; // Переход к последнему элементу
        }

        Apartments apartment = apartments.get(currentApartmentIndex);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(apartment.toString());

        // Создаем клавиатуру с кнопками
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();

        row2.add("Предыдущий");
        row2.add("Следующий");
        row3.add("Назад");

        keyboardRows.add(row2);
        keyboardRows.add(row3);

        keyboardMarkup.setKeyboard(keyboardRows);
        sendMessage.setReplyMarkup(keyboardMarkup);

        executeMessage(sendMessage);
    }

    private void nextApartment(long chatId) {
        currentApartmentIndex++; // Увеличиваем индекс
        showAllApartament(chatId); // Показываем обновленный список
    }

    private void previousApartment(long chatId) {
        currentApartmentIndex--; // Уменьшаем индекс
        showAllApartament(chatId); // Показываем обновленный список
    }

    private void sendMessage(long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(text);
        executeMessage(sendMessage);
    }

    private void openKeyBoardTofindApartaments(long chatId) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Нажми кнопку,чтобы выбрать какой поиск интересует");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        // Создаем первый ряд с двумя кнопками
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        row1.add("Фильтр");
        row2.add("Глобальный поиск");
        row3.add("Назад ◀️\uFE0F");
        keyboardRows.add(row1);
        keyboardRows.add(row2);
        keyboardRows.add(row3);


        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);

        executeMessage(message);
    }

    private void startAddingApartment(long chatId) {
        // Создаем новый объект Apartments и сохраняем его в мапе
        Apartments apartment = new Apartments();
        apartmentsMap.put(chatId, apartment);
        stepMap.put(chatId, 0);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Вы начали добавление квартиры. Пожалуйста, выберите район:");

        // Создаем клавиатуру
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        // Создаем первый ряд с кнопкой "Назад"
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Назад ◀️\uFE0F");

        keyboardRows.add(row1);
        keyboardMarkup.setKeyboard(keyboardRows);
        keyboardMarkup.setResizeKeyboard(true); // Уменьшает размер клавиатуры под кнопки

        message.setReplyMarkup(keyboardMarkup); // Устанавливаем клавиатуру в сообщение

        // Отправляем сообщение
        executeMessage(message);

        // Показываем кнопки районов
        showDistrictButtons(chatId);
    }


    private void handleDistrictSelection(long chatId, String district) {
        Apartments apartment = apartmentsMap.get(chatId);
        if (apartment != null) {
            apartment.setDistrict(district);
            sendMessage(chatId, "Район выбран: " + district);
            // Переходим к следующему шагу добавления объявления
            stepMap.put(chatId, 1); // Устанавливаем следующий шаг
            sendMessage(chatId, "Пожалуйста, выберите тип жилья:\n" +
                    "1 - Квартира\n" +
                    "2 - Дом\n" +
                    "3 - Участок\n" +
                    "4 - Котедж\n");
        } else {
            sendMessage(chatId, "Ошибка: объявление не найдено.");
        }
    }

    private void processApartmentInput(long chatId, String messageText, User user) {
        if (messageText.equals("Назад ◀️\uFE0F")) {
            apartmentsMap.remove(chatId);
            stepMap.remove(chatId);
            sendLoginAndRegisterButton(chatId);
            return;
        }

        Apartments apartment = apartmentsMap.get(chatId);
        if (apartment == null) {
            sendMessage(chatId, "Ошибка: объявление не найдено.");
            return;
        }

        int step = stepMap.get(chatId);

        switch (step) {
            case 0:
                apartment.setDistrict(messageText);
                stepMap.put(chatId, 1);
                sendMessage(chatId, "Пожалуйста, выберите тип жилья:\n" +
                        "1 - Квартира\n" +
                        "2 - Дом\n" +
                        "3 - Участок\n" +
                        "4 - Котедж\n");
                break;
            case 1:
                switch (messageText) {
                    case "1":
                        apartment.setName("Квартира");
                        break;
                    case "2":
                        apartment.setName("Дом");
                        break;
                    case "3":
                        apartment.setName("Участок");
                        break;
                    case "4":
                        apartment.setName("Котедж");
                        break;
                    default:
                        sendMessage(chatId, "Неверный выбор. Пожалуйста, выберите тип жилья из списка.");
                        return;
                }
                stepMap.put(chatId, 2);
                sendMessage(chatId, "Введите улицу:");
                break;
            case 2:
                apartment.setStreet(messageText);
                stepMap.put(chatId, 3);
                sendMessage(chatId, "Пожалуйста, введите номер:");
                break;
            case 3:
                apartment.setApartmentNumber(messageText);
                stepMap.put(chatId, 4);
                sendMessage(chatId, "Пожалуйста, введите цену:");
                break;
            case 4:
                try {
                    apartment.setPrice(Double.parseDouble(messageText));
                    stepMap.put(chatId, 5);
                    sendMessage(chatId, "Пожалуйста, введите номер телефона:");
                } catch (NumberFormatException e) {
                    sendMessage(chatId, "Пожалуйста, введите корректную цену.");
                }
                break;
            case 5:
                apartment.setPhoneNumber(messageText);
                stepMap.put(chatId, 6);
                sendMessage(chatId, "Пожалуйста, введите имя владельца:");
                break;
            case 6:
                apartment.setOwnerName(messageText);
                stepMap.put(chatId, 7);
                sendMessage(chatId, "Пожалуйста, введите описание:");
                break;
            case 7:
                apartment.setDescription(messageText);
                apartment.setUserId(chatId);
                saveApartment(chatId, apartment, user);
                apartmentsMap.remove(chatId);
                stepMap.remove(chatId);
                sendMessage(chatId, "Квартира успешно добавлена!");
                break;
            default:
                sendMessage(chatId, "Простите, но команда не известна");
                break;
        }
    }

    private void saveApartment(long chatId, Apartments apartment, User user) {
        apartmentsService.save(apartment);
    }

    private void sendHelpInfo(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("SmartAgentBot: Ваш умный помощник в мире недвижимости\n" +
                "\n" +
                "\uD83C\uDFE0 Обзор и поиск недвижимости\n" +
                "\n" +
                "    Поиск по параметрам: Легко находите апартаменты, дома, участки и коттеджи по вашим критериям: район, улица, цена и другие параметры.\n" +
                "\n" +
                "    Детальные описания: Каждое объявление содержит подробную информацию, включая вид недвижимости, район, улицу, цену, контакты владельца и описание.\n" +
                "\n" +
                "\uD83D\uDCDD Управление объявлениями\n" +
                "\n" +
                "    Добавление объявлений: Просто добавляйте свои объявления о продаже или аренде, указывая все необходимые детали.\n" +
                "\n" +
                "    Редактирование и удаление: Управляйте своими объявлениями: вносите изменения или удаляйте их по мере необходимости.\n" +
                "\n" +
                "\uD83D\uDD0D Персонализированный поиск\n" +
                "\n" +
                "    Фильтры поиска: Используйте фильтры для уточнения результатов поиска, чтобы найти именно то, что вам нужно.\n" +
                "\n" +
                "    Сохранение предпочтений: Бот запоминает ваши предпочтения и предлагает наиболее релевантные варианты.\n" +
                "\n" +
                "\uD83D\uDCF1 Удобство общения\n" +
                "\n" +
                "    Интерактивные кнопки: Используйте кнопки для быстрого выбора действий и ответов.\n" +
                "\n" +
                "    Поддержка Markdown и HTML: Читабельные и красиво оформленные сообщения для лучшего восприятия информации.\n" +
                "\n" +
                "\uD83D\uDD12 Безопасность и конфиденциальность\n" +
                "\n" +
                "    Защита данных: Ваши данные защищены, и бот соблюдает конфиденциальность всей информации.\n" +
                "\n" +
                "    Проверенные объявления: Только проверенные и актуальные объявления доступны в боте.\n" +
                "\n" +
                "Как начать использовать SmartAgentBot:\n" +
                "\n" +
                "  " +
                "\n" +
                "    Начните диалог с ботом, используя команду /start.\n" +
                "\n" +
                "    Исследуйте функционал: Воспользуйтесь кнопками и командами для поиска и управления объявлениями.\n" +
                "\n" +
                "SmartAgentBot — ваш надежный помощник в мире недвижимости, который сделает процесс поиска и управления объявлениями простым и удобным!\n");

        executeMessage(message);
    }

    private void sendInfoAboutUser(long chatId, User user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        // Создаем первый ряд с двумя кнопками
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Назад");
        row1.add("Мои объявления");
        keyboardRows.add(row1);


        keyboardMarkup.setKeyboard(keyboardRows);
        sendMessage.setReplyMarkup(keyboardMarkup);
        sendMessage.setText(
                "<pre>" +
                        "————————————————————————————————————————\n" +
                        " Информация о пользователе:\n" +
                        "   <b>Имя пользователя:</b> " + user.getUsername() + "\n" +
                        "   <b>ID:</b> " + user.getId() + "\n" +
                        "————————————————————————————————————————" +
                        "</pre>"
        );
        sendMessage.enableHtml(true); // Включаем поддержку HTML

        executeMessage(sendMessage);
    }

    private void sendLoginAndRegisterButton(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Нажми кнопку");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        // Создаем первый ряд с двумя кнопками
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Личный кабинет \uD83D\uDD14");
        row1.add("Поиск \uD83D\uDD0D");
        keyboardRows.add(row1);

        // Создаем второй ряд с двумя кнопками
        KeyboardRow row2 = new KeyboardRow();
        row2.add("Помощь \uD83C\uDD98");
        row2.add("Добавить ➕");
        keyboardRows.add(row2);

        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);

        executeMessage(message);
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void prepareAndSendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения: ", e);
        }
    }


}

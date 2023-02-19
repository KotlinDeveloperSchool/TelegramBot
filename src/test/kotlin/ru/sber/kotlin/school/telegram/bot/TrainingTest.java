package ru.sber.kotlin.school.telegram.bot;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.sber.kotlin.school.telegram.bot.controller.TrainingBot;
import ru.sber.kotlin.school.telegram.bot.game.GameSelector;
import ru.sber.kotlin.school.telegram.bot.repository.BotRedisRepository;
import ru.sber.kotlin.school.telegram.bot.repository.DictionaryRepository;
import ru.sber.kotlin.school.telegram.bot.repository.UserRepository;
import ru.sber.kotlin.school.telegram.bot.service.TrainingService;
import ru.sber.kotlin.school.telegram.bot.util.CustomSender;
import ru.sber.kotlin.school.telegram.bot.util.Predicates;

import java.io.IOException;

import static java.lang.String.format;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.telegram.abilitybots.api.db.MapDBContext.offlineInstance;
import static ru.sber.kotlin.school.telegram.bot.TestUtils.mockFullUpdate;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class TrainingTest {
    private static final User USER = new User(1L, "first", false);

    public static final int USER_ID = 1337;
    public static final long CHAT_ID = 1337L;
    public static final String CHAT_ID_STRING = "1337";

    public static final String TOKEN = "6075495599:AAFrXSv3DerEikjjbSCu44f0igVl3wrLOFo";
    public static final String NAME = "TestikNesticBot";

    private SilentSender silent;
    private MessageSender sender;

    private TrainingBot bot;

    @Mock
    private BotRedisRepository botRedisRepository;
    @Mock
    private TrainingService trainingService;
    @Mock
    UserRepository userRepository;
    @Mock
    DictionaryRepository dictionaryRepository;
    @Mock
    GameSelector gameSelector;
    @Autowired
    private Predicates predicates;
    private static DBContext db;

  @BeforeEach
    void setUp() {
      db = offlineInstance("test");
      bot = new TrainingBot(TOKEN, NAME, trainingService, predicates, botRedisRepository);
      bot.onRegister();

      silent = mock(SilentSender.class);
      sender = mock(MessageSender.class);
      bot.setCustomSender(new CustomSender(sender, silent, botRedisRepository));

      when(trainingService.getAllFromRedis(Mockito.any())).thenReturn(new SendMessage(CHAT_ID_STRING, "getAllFromRedis"));
      when(trainingService.getGameStyles(Mockito.any())).thenReturn(new SendMessage(CHAT_ID_STRING, "getGameStyles"));
    }

    @Test
    public void test() {

        Update update = mockFullUpdate(bot, USER, "/menu");
        bot.onUpdateReceived(update);
        verify(silent, times(1)).send(format("Sorry, this feature requires %d additional inputs.", 4), USER.getId());
    }

    @AfterEach
    public void tearDown() throws IOException {
        db.clear();
        db.close();
    }

}

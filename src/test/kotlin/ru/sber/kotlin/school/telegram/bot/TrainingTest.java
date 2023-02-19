package ru.sber.kotlin.school.telegram.bot;

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

import static org.mockito.Mockito.*;

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
    private DBContext db;

    @BeforeEach
    void setUp() {
//        db = offlineInstance("db");
        bot = new TrainingBot(TOKEN, NAME, trainingService, predicates, botRedisRepository);
        bot.onRegister();
        silent = mock(SilentSender.class);
        sender = mock(MessageSender.class);
        bot.setCustomSender(new CustomSender(sender, silent, botRedisRepository));
        when(trainingService.getAllFromRedis(Mockito.any())).thenReturn(new SendMessage(CHAT_ID_STRING, "getAllFromRedis"));
        when(trainingService.getGameStyles(Mockito.any())).thenReturn(new SendMessage(CHAT_ID_STRING, "getGameStyles"));
    }

    @Test
    public void canSayHelloWorld() {
        Update upd = new Update();
        upd.getMessage().getFrom().setId(CHAT_ID);


        // Create a new User - User is a class similar to Telegram User
        User user = new User();
        // This is the context that you're used to, it is the necessary conumer item for the ability
        // MessageContext context = MessageContext.newContext(upd, user, CHAT_ID);

        // We consume a context in the lamda declaration, so we pass the context to the action logic
        bot.showFavoriteDicts().action().accept(bot, upd);

        // We verify that the silent sender was called only ONCE and sent Hello World to CHAT_ID
        // The silent sender here is a mock!
        Mockito.verify(silent, times(1)).send("Hello World!", CHAT_ID);
    }


}

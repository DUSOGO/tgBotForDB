package jdbc;

import jdbc.utils.ConnectionManager;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcRunner {

    public boolean insertUser(Message infoFromMessage) {
        String first_name = infoFromMessage.getFrom().getFirstName();
        String username = infoFromMessage.getFrom().getUserName();
        String chat_id = "" + infoFromMessage.getChatId();
        String sql = """
                INSERT INTO users (first_name, username, chat_id)
                VALUES ('%s', '%s', %s);
                """.formatted(first_name, username, chat_id);

        return executeUpdate(sql);
    }

    public boolean addTest(Message infoFromMessage) {
        String username = infoFromMessage.getFrom().getUserName();
        String sql = """
                INSERT INTO tests (usid)
                VALUES ((SELECT id FROM users WHERE username = '%s'));
                """.formatted(username);

        if (!executeUpdate(sql)) { // if user don't have test
            deleteTest(infoFromMessage);
            addTest(infoFromMessage);  //Re-call after deletion
        }

        return true;
    }


    private void deleteTest(Message infoFromMessage) {
        String username = infoFromMessage.getFrom().getUserName();
        String sql = """
                DELETE FROM tests
                WHERE usid = (SELECT id FROM users WHERE username = '%s');
                """.formatted(username);

        executeUpdate(sql);
    }

    public void шmain() {
        String sql = """
                select * from users
                """;
        try (var connection = ConnectionManager.open();
             var statement = connection.prepareStatement(sql)) {
            var result = statement.executeQuery();
            while (result.next()) {
                System.out.println(result.getString("first_name"));
                System.out.println();
            }
        } catch (SQLException e) {
            System.out.println("ПИЗДОС! МАЛАДЕЦ, ВСЁ НАКРЫЛОСЬ!!!!!!"); //error message
            throw new RuntimeException(e);
        }
    }

    private static boolean executeUpdate(String sql) {
        try (var connection = ConnectionManager.open();
             var statement = connection.createStatement()) {
            var result = statement.executeUpdate(sql);
            if (result == 1) { // if done
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.out.println("ПИЗДОС! МАЛАДЕЦ, ВСЁ НАКРЫЛОСЬ!!!!!! Не получилось добавить пользователя"); //error message
            System.out.println(e.getMessage());
            return false;
        }
    }
}
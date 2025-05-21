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

    public boolean addQuestion(Message infoFromMessage) {
        String username = infoFromMessage.getFrom().getUserName();
        String question = infoFromMessage.getText();
        String sql = """
                INSERT INTO questions (teid, question)
                VALUES ((SELECT id FROM tests WHERE usid = (SELECT id FROM users WHERE username = '%s')), '%s');
                """.formatted(username, question);
        return executeUpdate(sql);
    }

    public boolean addAnswers(String question, String answer1_true, String answer2,String answer3,String answer4) {
        String sql = """
                INSERT INTO answers (id, answer1_true, answer2, answer3, answer4)
                VALUES ((SELECT id FROM questions WHERE question = '%s'), '%s', '%s', '%s', '%s');
                """.formatted(question, answer1_true, answer2, answer3, answer4);
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
            // Попробовать добавить второй раз, но не рекурсивно!
            if (!executeUpdate(sql)) {
                // Здесь можно залогировать ошибку или вернуть false
                System.out.println("Не удалось создать тест даже после удаления старого!");
                return false;
            }
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
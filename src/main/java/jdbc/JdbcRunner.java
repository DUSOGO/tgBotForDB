package jdbc;

import jdbc.utils.ConnectionManager;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


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

    public int getTestId(long chatId) {
        String sql = """
        SELECT t.id
        FROM tests t
        JOIN users u ON t.usid = u.id
        WHERE u.chat_id = %d
        """.formatted(chatId);

        try (var conn = ConnectionManager.open();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new IllegalStateException("Test ID not found for chat_id: " + chatId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error while fetching test ID by chat ID", e);
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
            return false;
        }
    }

    public String[][] getTest(int testId) {
        String sql = """
        SELECT q.question, a.answer1_true, a.answer2, a.answer3, a.answer4
        FROM questions q
        JOIN answers a ON q.id = a.id
        WHERE q.teid = %d
        ORDER BY q.id
        """.formatted(testId);

        ArrayList<String[]> result = new ArrayList<>();
        try (var conn = ConnectionManager.open();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new String[]{
                        rs.getString("question"),
                        rs.getString("answer1_true"),
                        rs.getString("answer2"),
                        rs.getString("answer3"),
                        rs.getString("answer4")
                });
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении вопросов и ответов", e);
        }
        return result.toArray(new String[0][0]);
    }

    public long getChatIdOfTestOwner(int testId) {
        String sql = """
        SELECT u.chat_id
        FROM tests t
        JOIN users u ON t.usid = u.id
        WHERE t.id = %d
        """.formatted(testId);

        try (var conn = ConnectionManager.open();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong("chat_id");
            } else {
                throw new IllegalStateException("chat_id не найден для testId: " + testId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении chat_id по testId", e);
        }
    }
}
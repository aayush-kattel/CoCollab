package room;

import api.GroqService;
import db.ChallengeDAO;
import db.RoomDAO;
import models.Challenge;
import models.Room;

import java.util.List;
import java.util.Random;

public class RoomService {

    private RoomDAO roomDAO = new RoomDAO();
    private ChallengeDAO challengeDAO = new ChallengeDAO();
    private GroqService groqService = new GroqService();
    private Random random = new Random();

    public String generateCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        String code;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            code = sb.toString();
        } while (roomDAO.codeExists(code));
        return code;
    }

    public Room createRoom(int ownerId, String name, String topic, String language, String difficulty, int questionCount) {
        Room room = new Room();
        room.setOwnerId(ownerId);
        room.setName(name);
        room.setCode(generateCode());
        room.setTopic(topic);
        room.setLanguage(language);
        room.setDifficulty(difficulty);
        room.setStatus("waiting");
        int roomId = roomDAO.create(room);
        if (roomId < 0) {
            return null;
        }
        room.setId(roomId);
        roomDAO.addMember(roomId, ownerId, "owner");

        int count = questionCount > 0 ? questionCount : 10;
        List<Challenge> bundle = groqService.generateChallengeBundle(topic, difficulty, language, count, roomId, ownerId);
        for (Challenge ch : bundle) {
            challengeDAO.create(ch);
        }

        return roomDAO.findById(roomId);
    }

    public Room joinRoom(String code, int userId) {
        if (code == null) return null;
        code = code.trim().toUpperCase();
        Room room = roomDAO.findByCode(code);
        if (room == null) return null;
        if ("finished".equalsIgnoreCase(room.getStatus())) return null;
        roomDAO.addMember(room.getId(), userId, "member");
        return roomDAO.findById(room.getId());
    }
}
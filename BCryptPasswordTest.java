import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 简单的BCrypt密码测试
 */
public class BCryptPasswordTest {
    
    public static void main(String[] args) {
        // 数据库中存储的admin用户密码哈希值
        String storedPasswordHash = "$2a$12$fsdyyZAzWd1waqAMHn14oeTWrwkpzgo8M2VXKwbtTfX9wUoVg/4OK";
        
        // 创建BCryptPasswordEncoder实例
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 验证密码
        boolean matches = encoder.matches("123456", storedPasswordHash);
        
        // 输出结果
        System.out.println("密码 '123456' 与存储的哈希值匹配: " + matches);
        
        // 如果不匹配，尝试其他可能的密码
        if (!matches) {
            String[] possiblePasswords = {"admin", "password", "admin123", "123123"};
            for (String pwd : possiblePasswords) {
                boolean m = encoder.matches(pwd, storedPasswordHash);
                System.out.println("密码 '" + pwd + "' 与存储的哈希值匹配: " + m);
            }
        }
    }
}

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class H {
    public static void main(String[] args) {
        java.io.Console console=System.console();
        if(console==null) throw new IllegalStateException("Interactive console required");
        char[] secret=console.readPassword("Password: ");
        try {
            if(secret==null || secret.length<12) throw new IllegalArgumentException("Password too short");
            System.out.println(new BCryptPasswordEncoder().encode(new String(secret)));
        } finally { if(secret!=null) java.util.Arrays.fill(secret, '\0'); }
    }
}

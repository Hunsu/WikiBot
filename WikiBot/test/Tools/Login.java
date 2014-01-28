package Tools;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class Login {

	private String password;
	private String login;

	public Login(){
		String[] s;
		try {
			s = FileUtils.readFileToString(new File("login")).split("\\n");
			password = s[1];
			login = s[0];
		} catch (IOException e) {
			password = null;
			login = null;
		}

	}

	public String getLogin() {
		// TODO Auto-generated method stub
		return login;
	}

	public String getPassword() {
		return password;
	}

}

package alpha.undefined.botcare;
import android.graphics.Bitmap;

import java.io.Serializable;
import java.text.SimpleDateFormat;

import java.util.Date;

public class Message implements Serializable {

	String text, spec, cont; //Text, Speciality, Contact
	int type;
	Date timestamp;
	Bitmap photo;

	public Message(String text, int type) {
		this.text = text;
		this.type = type;
		this.timestamp = new Date();
	}


	public Message(Bitmap photo, int type) {
		this.photo = photo;
		this.type = type;
		this.timestamp = new Date();
	}


	public Message(String text, String spec, String cont, int type) {
		this.text = text;
		this.spec = spec;
		this.cont = cont;
		this.type = type;
		this.timestamp = new Date();
	}


	public String getText() {
		return text;
	}

	public int getType() {
		return type;
	}

	public Bitmap getPhoto() {
		return photo;
	}

	public String getTime() {
		SimpleDateFormat localDateFormat = new SimpleDateFormat("hh:mm a");
		return localDateFormat.format(timestamp);
	}

}

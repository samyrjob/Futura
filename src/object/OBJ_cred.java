package object;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;


public class OBJ_cred extends SuperObject {


    public OBJ_cred(){

        try {

            InputStream stream = getClass().getResourceAsStream("/res/object/GoldenCred.png");

            // Explicitly check if the resource is not found
            if (stream == null) {
                throw new IOException("Image not found! Check the path: /res/object/GoldenCred.png");
            }

            image = ImageIO.read(stream);

        }
        catch (IOException ioe){
            ioe.printStackTrace();
        }



    }
    
}

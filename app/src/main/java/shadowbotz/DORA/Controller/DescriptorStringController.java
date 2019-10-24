package shadowbotz.DORA.Controller;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;

import shadowbotz.DORA.Model.Robot;

import static android.content.ContentValues.TAG;

public class DescriptorStringController {

    private ImageAdapter imageAdapter;
    private String originalDescriptorString1;
    private String originalDescriptorString2;
    private String originalArrowDescriptorString;
    private int numberOfExploredTiles = 0; //use this to check if there is any padding

    public DescriptorStringController(ImageAdapter imageAdapter) {
        this.imageAdapter = imageAdapter;
    }

    public void splitImageString(String arrowString) {
        if (arrowString != null && !arrowString.isEmpty()) {
            /*Example: (6, 5, D):(3, 9, R):(1, 15, D):(7, 19, L):(14, 14, U)*/
            arrowString = arrowString.substring(1, arrowString.length() - 1);
            String[] processedArrowString = arrowString.split("\\):\\(");

            for (String s : processedArrowString) {
                String[] temp = s.split(", ");
                int x = Integer.parseInt(temp[0]);
                int y = Integer.parseInt(temp[1]);

                if (temp[2].equals("U")) {
                    imageAdapter.mThumbIds[Math.abs(19 - y) * 15 + x] = 3;
                    imageAdapter.currentMapWithNoRobot[Math.abs(19 - y) * 15 + x] = 3;
                } else if (temp[2].equals("D")) {
                    imageAdapter.mThumbIds[Math.abs(19 - y) * 15 + x] = 4;
                    imageAdapter.currentMapWithNoRobot[Math.abs(19 - y) * 15 + x] = 4;
                } else if (temp[2].equals("L")) {
                    imageAdapter.mThumbIds[Math.abs(19 - y) * 15 + x] = 5;
                    imageAdapter.currentMapWithNoRobot[Math.abs(19 - y) * 15 + x] = 5;
                } else if (temp[2].equals("R")) {
                    imageAdapter.mThumbIds[Math.abs(19 - y) * 15 + x] = 6;
                    imageAdapter.currentMapWithNoRobot[Math.abs(19 - y) * 15 + x] = 6;
                } else if (temp[2].equals("1")) {
                    imageAdapter.mThumbIds[Math.abs(19 - y) * 15 + x] = 21;
                    imageAdapter.currentMapWithNoRobot[Math.abs(19 - y) * 15 + x] = 21;
                } else if (temp[2].equals("2")) {
                    imageAdapter.mThumbIds[Math.abs(19 - y) * 15 + x] = 22;
                    imageAdapter.currentMapWithNoRobot[Math.abs(19 - y) * 15 + x] = 22;
                } else if (temp[2].equals("3")) {
                    imageAdapter.mThumbIds[Math.abs(19 - y) * 15 + x] = 23;
                    imageAdapter.currentMapWithNoRobot[Math.abs(19 - y) * 15 + x] = 23;
                } else if (temp[2].equals("4")) {
                    imageAdapter.mThumbIds[Math.abs(19 - y) * 15 + x] = 24;
                    imageAdapter.currentMapWithNoRobot[Math.abs(19 - y) * 15 + x] = 24;
                } else if (temp[2].equals("5")) {
                    imageAdapter.mThumbIds[Math.abs(19 - y) * 15 + x] = 25;
                    imageAdapter.currentMapWithNoRobot[Math.abs(19 - y) * 15 + x] = 25;
                } else if (temp[2].equals("6")) {
                    imageAdapter.mThumbIds[Math.abs(19 - y) * 15 + x] = 20;
                    imageAdapter.currentMapWithNoRobot[Math.abs(19 - y) * 15 + x] = 20;
                } else if (temp[2].equals("7")) {
                    imageAdapter.mThumbIds[Math.abs(19 - y) * 15 + x] = 18;
                    imageAdapter.currentMapWithNoRobot[Math.abs(19 - y) * 15 + x] = 18;
                } else if (temp[2].equals("8")) {
                    imageAdapter.mThumbIds[Math.abs(19 - y) * 15 + x] = 17;
                    imageAdapter.currentMapWithNoRobot[Math.abs(19 - y) * 15 + x] = 17;
                } else if (temp[2].equals("9")) {
                    imageAdapter.mThumbIds[Math.abs(19 - y) * 15 + x] = 19;
                    imageAdapter.currentMapWithNoRobot[Math.abs(19 - y) * 15 + x] = 19;
                } else if (temp[2].equals("10")) {
                    imageAdapter.mThumbIds[Math.abs(19 - y) * 15 + x] = 26;
                    imageAdapter.currentMapWithNoRobot[Math.abs(19 - y) * 15 + x] = 26;
                } else if (temp[2].equals("11")) {
                    imageAdapter.mThumbIds[Math.abs(19 - y) * 15 + x] = 12;
                    imageAdapter.currentMapWithNoRobot[Math.abs(19 - y) * 15 + x] = 12;
                } else if (temp[2].equals("12")) {
                    imageAdapter.mThumbIds[Math.abs(19 - y) * 15 + x] = 13;
                    imageAdapter.currentMapWithNoRobot[Math.abs(19 - y) * 15 + x] = 13;
                } else if (temp[2].equals("13")) {
                    imageAdapter.mThumbIds[Math.abs(19 - y) * 15 + x] = 14;
                    imageAdapter.currentMapWithNoRobot[Math.abs(19 - y) * 15 + x] = 14;
                } else if (temp[2].equals("14")) {
                    imageAdapter.mThumbIds[Math.abs(19 - y) * 15 + x] = 15;
                    imageAdapter.currentMapWithNoRobot[Math.abs(19 - y) * 15 + x] = 15;
                } else if (temp[2].equals("15")) {
                    imageAdapter.mThumbIds[Math.abs(19 - y) * 15 + x] = 16;
                    imageAdapter.currentMapWithNoRobot[Math.abs(19 - y) * 15 + x] = 16;
                }
            }
            imageAdapter.notifyDataSetChanged();
        }
    }

    public void descriptorString1(String descriptorString1) {
        numberOfExploredTiles = 0;
        String padded = new BigInteger(descriptorString1, 16).toString(2);

        //pad string with leading zeros
        String formatPad = "%" + (descriptorString1.length() * 4) + "s";
        padded = String.format(formatPad, padded).replace(" ", "0");

        padded = padded.substring(2, padded.length() - 2);
        originalDescriptorString1 = padded;

        Integer[] integers = new Integer[padded.length()];
        // Creates the integer array.
        for (int i = 0; i < integers.length; i++) {
            integers[i] = Integer.parseInt(String.valueOf(padded.charAt(((19 - Math.abs(i / 15)) * 15) + (i % 15)))); //((19-Math.abs(i/15))*15) + (i%15)) =>to convert the axis
            if (integers[i] == 1) {
                numberOfExploredTiles++;
            }
        }
//        imageAdapter.mThumbIds = integers;
        System.arraycopy(integers, 0, imageAdapter.currentMapWithNoRobot, 0, integers.length);
        imageAdapter.notifyDataSetChanged();
    }

    public void descriptorString2(String descriptorString2) {
        String padded = new BigInteger(descriptorString2, 16).toString(2);

        //pad string with leading zeros
        String formatPad = "%" + (descriptorString2.length() * 4) + "s";
        padded = String.format(formatPad, padded).replace(" ", "0");

        /*Ensure padding is at the back*/ //TODO: check if padding works
        int numOfPaddings = padded.length() - numberOfExploredTiles;
        padded = padded.substring(0, padded.length() - numOfPaddings);

        int count = 0;
        char[] charOfOriginalDescriptorString = originalDescriptorString1.toCharArray();

        for (int i = 0; i < originalDescriptorString1.length(); i++) {
            if (String.valueOf(charOfOriginalDescriptorString[i]).equals("1")) {
                if (String.valueOf(padded.charAt(count)).equals("1")) {
                    charOfOriginalDescriptorString[i] = '2';
                }
                count++;
            }
        }
        String temp = String.valueOf(charOfOriginalDescriptorString);

        Integer[] integers = new Integer[temp.length()];
        // Creates the integer array.
        for (int i = 0; i < integers.length; i++) {
            integers[i] = Integer.parseInt(String.valueOf(temp.charAt(((19 - Math.abs(i / 15)) * 15) + (i % 15)))); //((19-Math.abs(i/15))*15) + (i%15)) =>to convert the axis
        }
//        imageAdapter.mThumbIds = integers;
        System.arraycopy(integers, 0, imageAdapter.mThumbIds, 0, integers.length);
        System.arraycopy(integers, 0, imageAdapter.currentMapWithNoRobot, 0, integers.length);
        imageAdapter.notifyDataSetChanged();
    }

    public void checkIfWaypointVisited(Robot robot) {  //For auto updating the map with the waypoint
        if (robot.getWaypointPosition() != 0) {
            if (imageAdapter.currentMapWithNoRobot[robot.getWaypointPosition()] == 1) {
                imageAdapter.mThumbIds[robot.getWaypointPosition()] = 11; //explored waypoint
            } else {
                imageAdapter.mThumbIds[robot.getWaypointPosition()] = 10; //unexplored waypoint
            }
            imageAdapter.notifyDataSetChanged();
        }

    }

    public void updateRobotPosition(Robot robot) { //For auto updating the map with robot
        //TODO: Require Rpi to send position of head and center of body for every movement

        imageAdapter.mThumbIds[robot.getBody()] = 8;
        //4 corners
        imageAdapter.mThumbIds[robot.getBody() - 14] = 8; //set the whole body
        imageAdapter.mThumbIds[robot.getBody() - 16] = 8;
        imageAdapter.mThumbIds[robot.getBody() + 14] = 8;
        imageAdapter.mThumbIds[robot.getBody() + 16] = 8;

        //the rest
        imageAdapter.mThumbIds[robot.getBody() + 1] = 8;
        imageAdapter.mThumbIds[robot.getBody() - 1] = 8;
        imageAdapter.mThumbIds[robot.getBody() + 15] = 8;
        imageAdapter.mThumbIds[robot.getBody() - 15] = 8;

        imageAdapter.mThumbIds[robot.getHead()] = 9; //color the head of robot

        imageAdapter.notifyDataSetChanged();
    }


    public void processJSONDescriptorString(JSONObject jsonObject, Robot robot) {
        /*Whole block here should be called when receiving descriptor string from Rpi*/
        if (jsonObject != null) {
            try {
                descriptorString1(jsonObject.getString("map"));
                descriptorString2(jsonObject.getString("obstacle"));
//                String test3 = jsonObject.getString("arrows");
//                String test = test3;
                //testing setting of arrows
                if (jsonObject.has("arrows")) {
                    splitImageString(jsonObject.getString("arrows"));
                }

                String head = jsonObject.getString("robotHead");
                head = head.substring(1, head.length() - 1);
                String[] process_head = head.split(", ");

                int x_head = Integer.parseInt(process_head[0]);
                int y_head = Integer.parseInt(process_head[1]);

                robot.setHead(Math.abs(19 - y_head) * 15 + x_head);

                String body = jsonObject.getString("robotCenter");
                body = body.substring(1, body.length() - 1);
                String[] process_body = body.split(", ");
                int x_body = Integer.parseInt(process_body[0]);
                int y_body = Integer.parseInt(process_body[1]);

                robot.setBody(Math.abs(19 - y_body) * 15 + x_body);

                updateRobotPosition(robot);
//                b'ROBOT:{"map": "FFC07F80FF01FE03FFFFFFF3FFE7FFCFFF9C7F38FE71FCE3F87FF0FFE1FFC3FF87FF0E0E1C1F",
//                "obstacle": "00000000001C80000000001C0000080000060001C00000000000",
//                        "robotCenter": "(1, 1)",
//                        "robotHead": "(2, 1)",
//                        "fpath":"(4, 1, f):(5, 1, f):(6, 1, f):(7, 1, f):(8, 1, f):(9, 1, f):(9, 2, f):(3, 1, f):(9, 2, f):(9, 3, f)"}'
                checkIfWaypointVisited(robot);
            } catch (JSONException e) {
                Log.d(TAG, "processJSONDescriptorString: " + e);
            }
        }

    }
}

//
//    public void highlight(String arrowString) {
//        if (arrowString != null && !arrowString.isEmpty()) {
//            /*Example: (6, 5, D):(3, 9, R):(1, 15, D):(7, 19, L):(14, 14, U)*/
//            arrowString = arrowString.substring(1, arrowString.length() - 1);
//            String[] processedArrowString = arrowString.split("\\):\\(");
//
//            for (String s : processedArrowString) {
//                String[] temp = s.split(", ");
//                int x = Integer.parseInt(temp[0]);
//                int y = Integer.parseInt(temp[1]);
//                imageAdapter.mThumbIds[Math.abs(19 - y) * 15 + x] = 27;
//                imageAdapter.currentMapWithNoRobot[Math.abs(19 - y) * 15 + x] = 27;
//            }
//        }
//    }


//b'ROBOT:{"map": "FFC07F80FF01FE03FFFFFFF3FFE7FFCFFF9C7F38FE71FCE3F87FF0FFE1FFC3FF87FF0E0E1C1F",
//        "obstacle": "00000100001C80000000001C0000080000060001C00000080000",
//        "robotCenter": "(1, 1)",
//        "robotHead": "(1, 2)",
//        "arrows": "(6, 5, 5):(3, 9, 1):(1, 15, 2):(7, 19, 3):(14, 14, 4)",
//        "fpath": "(3,1,F):(4,1,F):(5,1,F):(6,1,F):(7,1,F)"}'




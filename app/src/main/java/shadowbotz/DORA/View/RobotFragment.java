package shadowbotz.DORA.View;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.andretietz.android.controller.DirectionView;
import com.andretietz.android.controller.InputView;

import org.json.JSONException;
import org.json.JSONObject;

import shadowbotz.DORA.BluetoothObserverSubject.Observer;
import shadowbotz.DORA.BluetoothObserverSubject.Subject;
import shadowbotz.DORA.Config;
import shadowbotz.DORA.Controller.DescriptorStringController;
import shadowbotz.DORA.Controller.ImageAdapter;
import shadowbotz.DORA.Controller.MovementController;
import shadowbotz.DORA.Model.Robot;
import shadowbotz.DORA.Model.BluetoothMessage;
import shadowbotz.DORA.R;

import static android.content.Context.SENSOR_SERVICE;

public class RobotFragment extends Fragment implements Observer {


    private Robot robot = new Robot();

    // Observer pattern
    private Subject topic;
    private MovementController movementController;
    private TextView statusTextView;
    private ImageAdapter imageAdapter;

    private Button buttonSetWaypoint;
    private Button buttonSetRobot;
    private Button buttonSendCoords;

    private boolean autoUpdate=true;
    private boolean canSetWayPoint = false;
    private boolean canSetRobot = false;
    private boolean rotateSensorOn = false; // Change to false to deactivate

    private Thread threadToSendAction = null;
    private boolean stopSendingAction = false;
    private String directionOfAction;

    private JSONObject latestGridAction;

    private DescriptorStringController descriptorStringController;

    private TextView textviewRobotBody;
    private TextView textviewRobotHead;
    private Switch switchRotateBySensor;
    private Switch switchNavigation;
    private LinearLayout linearLayoutForL1L2;

    private boolean competitiveMode = true;

    private Button buttonStart;
    private Button buttonStop;

    private RadioButton radioExploration;
    private RadioButton radioFastestPath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_robot, container, false);

        // Observer pattern
        MainActivity.bluetoothSubject.register(this);
        this.setSubject(MainActivity.bluetoothSubject);

        // Get Fragment belonged Activity
        final FragmentActivity fragmentBelongActivity = getActivity();
        final FloatingActionButton fab =  view.findViewById(R.id.fab);
        final SharedPreferences sharedPreferences = fragmentBelongActivity.getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        final DirectionView directionView = view.findViewById(R.id.viewDirection);

        switchNavigation = view.findViewById(R.id.switchNavigation);
        final Switch switchUpdate = view.findViewById(R.id.switchUpdate);

        buttonStart = view.findViewById(R.id.buttonStart);
        buttonStop = view.findViewById(R.id.buttonStop);

        radioExploration = view.findViewById(R.id.radioExploration);
        radioFastestPath = view.findViewById(R.id.radioFastestPath);

        textviewRobotBody = view.findViewById(R.id.textview_robot_body);
        textviewRobotHead = view.findViewById(R.id.textview_robot_head);
        final TextView textviewWaypoint = view.findViewById(R.id.textview_waypoint);

        final RelativeLayout leftColumn = view.findViewById(R.id.leftColumn);
        final LinearLayout rightColumn = view.findViewById(R.id.rightColumn);

        switchRotateBySensor = view.findViewById(R.id.switchRotateBySensor);
        linearLayoutForL1L2 = view.findViewById(R.id.linearLayoutforL1L2);

        switchRotateBySensor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                rotateSensorOn = b;
            }
        });

        buttonSetWaypoint = view.findViewById(R.id.button_way_point);
        buttonSetRobot = view.findViewById(R.id.button_robot_position);
        buttonSendCoords = view.findViewById(R.id.button_send_coords);
        buttonSendCoords.setEnabled(false);

        statusTextView = view.findViewById(R.id.statusTextView);

        final Button buttonL1 = view.findViewById(R.id.buttonL1);
        final Button buttonL2 = view.findViewById(R.id.buttonL2);

        imageAdapter = new ImageAdapter(fragmentBelongActivity);

        movementController = new MovementController(imageAdapter, getActivity());

        SensorManager sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        Sensor rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        SensorEventListener rvListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if(rotateSensorOn && robot.isHeadPosition() && robot.isBodyPosition()){
                    float[] rotationMatrix = new float[16];
                    SensorManager.getRotationMatrixFromVector(
                            rotationMatrix, sensorEvent.values);

                    // Remap coordinate system
                    float[] remappedRotationMatrix = new float[16];
                    SensorManager.remapCoordinateSystem(rotationMatrix,
                            SensorManager.AXIS_X,
                            SensorManager.AXIS_Z,
                            remappedRotationMatrix);

                    // Convert to orientations
                    float[] orientations = new float[3];
                    SensorManager.getOrientation(remappedRotationMatrix, orientations);

                    for(int i = 0; i < 3; i++) {
                        orientations[i] = (float)(Math.toDegrees(orientations[i]));
                    }

                    if(orientations[2] > -70) {
                        movementController.turnRight(robot);
                        MainActivity.sendMessage("right");
                    } else if(orientations[2] < -110) {
                        movementController.turnLeft(robot);
                        MainActivity.sendMessage("left");
                    }
                }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        // Register it
        sensorManager.registerListener(rvListener,
                rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);

        //for onCreateOptionsMenu
        setHasOptionsMenu(true);

        GridView gridview = view.findViewById(R.id.gridview);
        gridview.setAdapter(imageAdapter);

        descriptorStringController = new DescriptorStringController(imageAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() { //set robot body and head
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                if(canSetRobot){
                    if((position%15>=1 && position%15<=13) && Math.abs(19-(Math.abs(position/15))) >=1 && Math.abs(19-(Math.abs(position/15))) <=18){
                        if(!robot.isBodyPosition()){
                            robot.setBody(position);
                            movementController.setBody(robot); // Set the starting position of the robot
                            robot.setBodyPosition(true);
                            // MainActivity.sendMessage("Way point: " +robot.getBody()%15 +", "+ Math.abs(19-(Math.abs(position/15))));

                            String output = robot.getBody()%15 +", "+ Math.abs(19-(Math.abs(position/15)));
                            textviewRobotBody.setText(output);
                        }
                        else if(!robot.isHeadPosition()){
                            robot.setHead(position);
                            if(robot.getHead() == robot.getBody()+1 || robot.getHead() == robot.getBody()-1 || robot.getHead() == robot.getBody()+15 || robot.getHead() == robot.getBody()-15 ){  //make sure head is at the correct position
                                movementController.setHead(robot);
                                robot.setHeadPosition(true);
                                // MainActivity.sendMessage("Way point: " +robot.getHead()%15 +", "+ Math.abs(19-(Math.abs(position/15))));

                                canSetRobot = false;
                                buttonSetWaypoint.setEnabled(true);
                                buttonSendCoords.setEnabled(true);

                                leftColumn.setVisibility(View.VISIBLE);
                                rightColumn.setVisibility(View.VISIBLE);

                                String output = robot.getHead()%15 +", "+ Math.abs(19-(Math.abs(position/15)));
                                textviewRobotHead.setText(output);
                            }
                            else{
                                Toast.makeText(fragmentBelongActivity, "Invalid position for head!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else{
                            Toast.makeText(fragmentBelongActivity, "Position of the robot has been not set!", Toast.LENGTH_SHORT).show();
                        }
                        imageAdapter.notifyDataSetChanged();

                    }
                    else{
                        Toast.makeText(fragmentBelongActivity, "Out of range!", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(fragmentBelongActivity, "Please click set robot button", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // Set way point
        gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (canSetWayPoint){
                    robot.setWaypointPosition(i);
                    movementController.setWayPoint(robot, statusTextView);
                    robot.setWaypoint(true);
                    canSetWayPoint = false;
                    buttonSetRobot.setEnabled(true);

                    leftColumn.setVisibility(View.VISIBLE);
                    rightColumn.setVisibility(View.VISIBLE);

                    String output = robot.getWaypointPosition()%15  +", "+Math.abs(19-(Math.abs(robot.getWaypointPosition()/15)));
                    textviewWaypoint.setText(output);
                }
                else{
                    Toast.makeText(fragmentBelongActivity, "Please click set waypoint button!", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        buttonSetWaypoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(robot.isWaypoint()){
                    movementController.clearWayPoint(robot);
                }
                canSetWayPoint = true;
                robot.setWaypoint(false);
                buttonSetRobot.setEnabled(false);
                leftColumn.setVisibility(View.INVISIBLE);
                rightColumn.setVisibility(View.INVISIBLE);
            }
        });

        buttonSetRobot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (robot.isBodyPosition()){
                    movementController.clearRobot(robot);
                }
                canSetRobot = true;
                robot.setHeadPosition(false);
                robot.setBodyPosition(false);
                buttonSetWaypoint.setEnabled(false);
                leftColumn.setVisibility(View.INVISIBLE);
                rightColumn.setVisibility(View.INVISIBLE);
            }
        });

        buttonSendCoords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject data = new JSONObject();

                // try {
                //     data.put("robot_head_x", robot.getHead()%15);
                //     data.put("robot_head_y", Math.abs(19-(Math.abs(robot.getHead()/15))));
                //     data.put("robot_body_x", robot.getBody()%15);
                //     data.put("robot_body_y", Math.abs(19-(Math.abs(robot.getBody()/15))));
                //     data.put("waypoint_x", robot.getWaypointPosition()%15);
                //     data.put("waypoint_y", Math.abs(19-(Math.abs(robot.getWaypointPosition()/15))));
                // } catch (JSONException e) {
                //     e.printStackTrace();
                // }
                //
                // JSONObject jsonObject = new JSONObject();
                //
                // try {
                //     jsonObject.put("coordinate", data);
                // } catch (JSONException e) {
                //     e.printStackTrace();
                // }
                //
                // MainActivity.sendMessage(jsonObject.toString());

                MainActivity.sendMessage("WP:"+robot.getWaypointPosition()%15+":"+Math.abs(19-(Math.abs(robot.getWaypointPosition()/15))));
                buttonStart.setEnabled(true);
                rightColumn.setVisibility(View.VISIBLE);
            }
        });

        directionView.setOnButtonListener(new InputView.InputEventListener() {
            @Override public void onInputEvent(View view, int buttons) {
                if(robot.isBodyPosition() && robot.isHeadPosition()){
                    switch (buttons&0xff) {
                        case DirectionView.DIRECTION_DOWN:
                            break;
                        case DirectionView.DIRECTION_RIGHT:
                            startActionThread();
                            directionOfAction = "right";
                            break;
                        case DirectionView.DIRECTION_LEFT:
                            startActionThread();
                            directionOfAction = "left";
                            break;
                        case DirectionView.DIRECTION_UP:
                            startActionThread();
                            directionOfAction = "forward";
                            break;
                        default:
                            stopSendingAction = true;
                            break;
                    }
                }
                else{
                    Toast.makeText(fragmentBelongActivity, "Robot has not been set!", Toast.LENGTH_SHORT).show();
                }

            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(latestGridAction != null) {
                    try {
                        descriptorStringController.processJSONDescriptorString(latestGridAction.getJSONObject("robot"), robot);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        switchUpdate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    fab.setVisibility(View.GONE);
                    fab.setActivated(false);
                    autoUpdate = true;
                }
                else{
                    fab.setVisibility(View.VISIBLE);
                    fab.setActivated(true);
                    autoUpdate = false;
                }
            }
        });

        switchNavigation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){ // if auto
                    LinearLayout linearLayoutForAutoNavigate = view.findViewById(R.id.linearLayoutForAutoNavigate);
                    linearLayoutForAutoNavigate.setVisibility(View.VISIBLE);

                    LinearLayout linearLayoutForDirectionalPad = view.findViewById(R.id.linearLayoutForDirectionalPad);
                    linearLayoutForDirectionalPad.setVisibility(View.GONE);
                }
                else{

                    LinearLayout linearLayoutForAutoNavigate = view.findViewById(R.id.linearLayoutForAutoNavigate);
                    linearLayoutForAutoNavigate.setVisibility(View.GONE);

                    LinearLayout linearLayoutForDirectionalPad = view.findViewById(R.id.linearLayoutForDirectionalPad);
                    linearLayoutForDirectionalPad.setVisibility(View.VISIBLE);
                }
            }
        });

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonStart.setVisibility(View.GONE);
                buttonStop.setVisibility(View.VISIBLE);

                if(radioExploration.isChecked()) {
                    MainActivity.sendMessage(Config.algorithm_start_exploration);
                }
                else if(radioFastestPath.isChecked()){
                    MainActivity.sendMessage(Config.algorithm_start_fastest_path);
                }

                radioExploration.setEnabled(false);
                radioFastestPath.setEnabled(false);
                switchNavigation.setEnabled(false);
                buttonSetWaypoint.setEnabled(false);
                buttonSetRobot.setEnabled(false);
                buttonSendCoords.setEnabled(false);
                buttonL1.setEnabled(false);
                buttonL2.setEnabled(false);
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(fragmentBelongActivity, "Please hold Stop button to terminate Auto Navigation!", Toast.LENGTH_SHORT).show();
            }
        });

        buttonStop.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                buttonStart.setVisibility(View.VISIBLE);
                buttonStop.setVisibility(View.GONE);

                MainActivity.sendMessage(Config.algorithm_stop);

                radioExploration.setEnabled(true);
                radioFastestPath.setEnabled(true);
                switchNavigation.setEnabled(true);
                buttonSetWaypoint.setEnabled(true);
                buttonSetRobot.setEnabled(true);
                buttonSendCoords.setEnabled(true);
                buttonL1.setEnabled(true);
                buttonL2.setEnabled(true);
                return false;
            }
        });

        buttonL1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String output = sharedPreferences.getString(Config.F1_BUTTON, "");
                MainActivity.sendMessage(output);
                Toast.makeText(fragmentBelongActivity, output, Toast.LENGTH_SHORT).show();
            }
        });

        buttonL2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String output = sharedPreferences.getString(Config.F2_BUTTON, "");
                MainActivity.sendMessage(output);
                Toast.makeText(fragmentBelongActivity, output, Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_toggle_competitive_mode) {
            if(competitiveMode) {
                switchRotateBySensor.setVisibility(View.VISIBLE);
                switchNavigation.setVisibility(View.VISIBLE);
                linearLayoutForL1L2.setVisibility(View.VISIBLE);
            }
            else {
                switchRotateBySensor.setVisibility(View.GONE);
                switchNavigation.setVisibility(View.GONE);
                linearLayoutForL1L2.setVisibility(View.GONE);
            }

            competitiveMode = !competitiveMode;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void update() {
        BluetoothMessage bluetoothMessage = (BluetoothMessage) topic.getUpdate(this);

        if (bluetoothMessage.getMessage().equals("endesp")) {
            buttonStart.setVisibility(View.VISIBLE);
            buttonStop.setVisibility(View.GONE);

            radioExploration.setEnabled(true);
            radioFastestPath.setEnabled(true);
            switchNavigation.setEnabled(true);
            buttonSetWaypoint.setEnabled(true);
            buttonSetRobot.setEnabled(true);
            buttonSendCoords.setEnabled(true);
        }
        else{
            try {
                String message = bluetoothMessage.getMessage();
                String y = message.substring(0, message.length()-1);
                // message.length()-1 for the original
                String s = y.replace("b'ROBOT:", "");
                String t = "{ \"robot\":\n";
                String end = "}";
                String u = t + s;
                String end1 = u + end;
                JSONObject msg = new JSONObject(end1);
//                String to receive from rpi
//               b'ROBOT:{
//                         "map": "FFC07F80FF01FE03FFFFFFF3FFE7FFCFFF9C7F38FE71FCE3F87FF0FFE1FFC3FF87FF0E0E1C1F",
//                         "obstacle": "00000100001C80000000001C0000080000060001C00000080000",
//                         "robotCenter": "(1, 1)",
//                         "robotHead": "(2, 1)",
//                         "arrows": "(6, 5, 1):(3, 9, 2):(1, 15, 3):(7, 19, 4):(14, 14, 5)"
//                 }'
                if (msg != null) {
                    try {
                        if (msg.getString("message") != null && msg.getString("message").length() > 0) {
                            // To identify the message (JSON Format)
                            // { "message": "<message>" }
                            statusTextView.setText(msg.getString("message"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                 /* Format of the string to retrieve from Rpi
                        { "robot":
                            {
                                "map": "FFC07F80FF01FE03FFFFFFF3FFE7FFCFFF9C7F38FE71FCE3F87FF0FFE1FFC3FF87FF0E0E1C1F",
                                "obstacle": "00000100001C80000000001C0000080000060001C00000080000",
                                "arrows": "(6, 5, D),(3, 9, R),(1, 15, D),(7, 19, L),(14, 14, U)",
                                "robotCenter": "(1, 1)",
                                "robotHead": "(2, 1)"
                            }
                        }
                       */
                        if (msg.getString("robot") != null && msg.getString("robot").length() > 0) {
                            latestGridAction = msg;
                            statusTextView.setText("Map:" + "\n" + msg.getJSONObject("robot").getString("map") + "\n" + "Obstacle:" + "\n" + msg.getJSONObject("robot").getString("obstacle"));

                            if (autoUpdate) {
                                descriptorStringController.processJSONDescriptorString(msg.getJSONObject("robot"), robot);

                                updateHeadBodyCoordinates();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        if (msg.getString("action") != null && msg.getString("action").length() > 0) {
                            if (robot.isHeadPosition() && robot.isBodyPosition()) {
                                try {
                                    statusTextView.setText(msg.getString("action"));
                                    switch (msg.getString("action")) {
                                        case "right":
                                            movementController.turnRight(robot);
                                            break;
                                        case "left":
                                            movementController.turnLeft(robot);
                                            break;
                                        case "forward":
                                            movementController.moveForward(robot);
                                            break;
                                    }
                                    updateHeadBodyCoordinates();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setSubject(Subject sub) {
        this.topic = sub;
    }

    public void startActionThread() {
        stopSendingAction = false;

        if(threadToSendAction != null && threadToSendAction.isAlive()) {
        }
        else {
            threadToSendAction = new Thread() {
                public void run() {
                    while (!stopSendingAction) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                switch (directionOfAction) {
                                    case ("left"):
                                        movementController.turnLeft(robot);
                                        MainActivity.sendMessage(Config.arduino_turn_left_command);
                                        break;
                                    case ("right"):
                                        movementController.turnRight(robot);
                                        MainActivity.sendMessage(Config.arduino_turn_right_command);
                                        break;
                                    case ("forward"):
                                        movementController.moveForward(robot);
                                        MainActivity.sendMessage(Config.arduino_move_forward_command);
                                        break;
                                    default:
                                        break;
                                }

                                updateHeadBodyCoordinates();
                            }
                        });

                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            threadToSendAction.start();
        }
    }

    private void updateHeadBodyCoordinates() {
        if(textviewRobotHead != null && textviewRobotBody != null){
            String robotHead = robot.getHead() % 15 + "," + Math.abs(19 - (Math.abs(robot.getHead() / 15)));
            String robotBody = robot.getBody() % 15 + "," + Math.abs(19 - (Math.abs(robot.getBody() / 15)));

            textviewRobotHead.setText(robotHead);
            textviewRobotBody.setText(robotBody);
        }
    }
}

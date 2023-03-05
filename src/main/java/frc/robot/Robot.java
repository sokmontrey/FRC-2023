// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

/* 
 * Sokmontrey Sythat
 * 2023 
 * 
 * - The "_" (underscore) infront of variables indicated that the variable is object's memeber
 * - Variable name are snake_case
 * - Method name are camelCase 
 * 
 * TODO: try using smart dashboard for variable robot setting
 * TODO: Use Camera
*/

package frc.robot;

//import all the nessary library to control the robot
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.motorcontrol.PWMVictorSPX;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;

public class Robot extends TimedRobot {
  //declare & define PWMVictorSPX drivers object for the left motors(front and back)
  private PWMVictorSPX _left_drive1 = new PWMVictorSPX(0);
  private PWMVictorSPX _left_drive2 = new PWMVictorSPX(1);

  //declare & define PWMVictorSPX drivers object for the right motors(front and back)
  private PWMVictorSPX _right_drive1 = new PWMVictorSPX(3);
  private PWMVictorSPX _right_drive2 = new PWMVictorSPX(2);

  //declare & define PWMVictorSPX drivers object for the intake motor
  private PWMVictorSPX _intake1 = new PWMVictorSPX(4);

  //define and declare Controller (XboxController & JoyStick)
  private XboxController _stick = new XboxController(0);
  private Joystick _stick2 = new Joystick(1);

  //will be multiplied the raw speed to lower the robot's speed
  // 0 < max_speed_factor <= 1
  //Could be negative but everything will be inverted
  private double _max_speed_factor = 0.65;
  //max speed for autonomous stage
  private double _max_auto_speed = 0.5;

  //A variable for intake rotation 1=out, 0=stop, -1=in
  private double _intake_direction = 0;
  private double _intake_speed = 0.7;

  //create Timer object named timer for working with timing in autonomous stage
  Timer _timer;

  //When the robot start
  @Override
  public void robotInit() {
    //Invert the axis of the left motor
    //Because it is a mirror of the right motor
    _left_drive1.setInverted(true);
    _left_drive2.setInverted(true);

    //TODO: check of the intake motor needed to be inverted
    //_intake1.setInverted(true);
  }

  //Loop for teleop stage
  @Override
  public void teleopPeriodic() {
    if(_stick.getRightBumperPressed()){
      if(_max_speed_factor == 0.65) _max_speed_factor = 0.3;
      else _max_speed_factor = 0.65;
    }

    //listen for a certain XBox button to be pressed to change intake direction
    //Y for take out
    //A for take in
    //When none of the button is pressed, intake direction is 0
    if(_stick.getYButtonPressed()){
      _intake_direction = 1;
    }
    if(_stick.getBButtonPressed()){
      _intake_direction = -1;
    }
    if(_stick.getYButtonReleased() || _stick.getBButtonReleased()){
      _intake_direction = 0;
    }

    //listen for a certain JoyStick button to be pressed to change intake direction
    //2 for take in
    //1 for take out
    //When none of the button is pressed, intake direction is 0
    if(_stick2.getRawButtonPressed(2)){
      _intake_direction = 1;
    }
    if(_stick2.getRawButtonPressed(1)){
      _intake_direction = -1;
    }
    if(_stick2.getRawButtonReleased(1) || _stick2.getRawButtonReleased(2)){
      _intake_direction = 0;
    }

    //Setting intake motor to speed * direction constantly.
    _setIntake(_intake_direction * _intake_speed);

    //get Value from the Joystick X and Y axis (only the left side)
    //Combine both value together to get left and right motor speed (ARCADE drive)
    //Math.min & Math.max is to limit the range of the combination (addition and substract) to a value between -1 and 1
    double left_speed = Math.min(1, Math.max(-1, _stick.getLeftY() - _stick.getLeftX()));
    double right_speed = Math.min(1, Math.max(-1, _stick.getLeftY() + _stick.getLeftX()));

    //motor_speed * max_speed to lower the speed of the robot
    left_speed *= _max_speed_factor;
    right_speed *= _max_speed_factor;

    //set the speed value to thier corresponding motor
    _setLeft(left_speed);
    _setRight(right_speed);
  }

  //When the autonomouse stage start
  @Override 
  public void autonomousInit(){
    _timer = new Timer();
  }

  //Loop for the autonomous stage
  @Override 
  public void autonomousPeriodic(){
    /*
     * The robot will be given a game object at the beginning 
     * 1. Turn the robot 180 (TODO: TEST IF THE ROBOT TURN 180 DEGREE)
     * 2. Move forward until it reach the "grid"
     * 3. release the game object
     */
    _turnRight(2);
    _moveForward(3);
    _takeOut();
  }

  /*
   * Custom method for reuseability in controlling motor speed
   * The method name is self-explained
   */

  private void _stopAllMotor(){
    _setLeft(0);
    _setRight(0);
    _setIntake(0);
  }

  /*
   * Move the motor at a set speed for a certain duration
   * 1. Set the direction and speed fo the motor
   * 2. Wait for duration second
   * 3. Stop the motor
   */

  private void _turnLeft(double duration){
    _setLeft(-1 * _max_auto_speed);
    _setRight(1 * _max_auto_speed);
    _timer.delay(duration);
    _stopAllMotor();
  }
  private void _turnRight(double duration){
    _setLeft(1 * _max_auto_speed);
    _setRight(-1 * _max_auto_speed);
    _timer.delay(duration);
    _stopAllMotor();
  }
  private void _moveForward(double duration){
    _setLeft(1 * _max_auto_speed);
    _setRight(1 * _max_auto_speed);
    _timer.delay(duration);
    _stopAllMotor();
  }
  private void _moveBackward(double duration){
    _setLeft(-1 * _max_auto_speed);
    _setRight(-1 * _max_auto_speed);
    _timer.delay(duration);
    _stopAllMotor();
  }

  private void _takeIn(){
    _setIntake(-1 * _intake_speed);
    _timer.delay(2);
    _setIntake(0);
  }
  private void _takeOut(){
    _setIntake(1 * _intake_speed);
    _timer.delay(2);
    _setIntake(0);
  }

  /*
   * set_method to set corresponding motor speed
   */
  private void _setIntake(double speed){
    _intake1.set(speed);
  }
  private void _setLeft(double speed){
    _left_drive1.set(speed);
    _left_drive2.set(speed);
  }
  private void _setRight(double speed){
    _right_drive1.set(speed);
    _right_drive2.set(speed);
  }
}

// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.ctre.phoenix.sensors.Pigeon2;
import com.pathplanner.lib.auto.AutoBuilder;

import com.revrobotics.RelativeEncoder;

import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
  import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
  import edu.wpi.first.math.kinematics.SwerveModulePosition;
  import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

//import com.pathplanner.lib.auto.AutoBuilder;
//import com.pathplanner.lib.util.PathPlannerLogging;



public class SwerveSubsystem extends SubsystemBase {
  /** Creates a new SwerveSubsystem. */

  SwerveModulePosition[] moduleStates;
  private final SwerveModule frontLeft = new SwerveModule(
    Constants.OperatorConstants.LEFT_FRONT_SPEED_MOTOR_ID,      //can ID for speed
    Constants.OperatorConstants.LEFT_FRONT_DIRECTION_MOTOR_ID,  //can ID for direction
    Constants.LEFT_FRONT_SPEED_IS_REVERSED,                     //boolean for if the speed motor is set to reverse 
    Constants.LEFT_FRONT_DIRECTION_IS_REVERSED,                 //boolean for if the direction motor is set to reverse
    Constants.OperatorConstants.LEFT_FRONT_CANCODER_ID,         //can ID for cancoder
    Constants.LEFT_FRONT_RADIAN_OFFSET,                         //to correct for encoder, so 0 is actually pointing ahead
    Constants.LEFT_FRONT_CANCODER_IS_REVERSED
  );

  private final SwerveModule frontRight = new SwerveModule(
    Constants.OperatorConstants.RIGHT_FRONT_SPEED_MOTOR_ID,      //can ID for speed
    Constants.OperatorConstants.RIGHT_FRONT_DIRECTION_MOTOR_ID,  //can ID for direction
    Constants.RIGHT_FRONT_SPEED_IS_REVERSED,                     //boolean for if the speed motor is set to reverse 
    Constants.RIGHT_FRONT_DIRECTION_IS_REVERSED,                 //boolean for if the direction motor is set to reverse
    Constants.OperatorConstants.RIGHT_FRONT_CANCODER_ID,         //can ID for cancoder
    Constants.RIGHT_FRONT_RADIAN_OFFSET,                         //to correct for encoder, so 0 is actually pointing ahead
    Constants.RIGHT_FRONT_CANCODER_IS_REVERSED
  );

  private final SwerveModule backLeft = new SwerveModule(
    Constants.OperatorConstants.LEFT_BACK_SPEED_MOTOR_ID,      //can ID for speed
    Constants.OperatorConstants.LEFT_BACK_DIRECTION_MOTOR_ID,  //can ID for direction
    Constants.LEFT_BACK_SPEED_IS_REVERSED,                     //boolean for if the speed motor is set to reverse 
    Constants.LEFT_BACK_DIRECTION_IS_REVERSED,                 //boolean for if the direction motor is set to reverse
    Constants.OperatorConstants.LEFT_BACK_CANCODER_ID,         //can ID for cancoder
    Constants.LEFT_BACK_RADIAN_OFFSET,                         //to correct for encoder, so 0 is actually pointing ahead
    Constants.LEFT_BACK_CANCODER_IS_REVERSED
  );

  private final SwerveModule backRight = new SwerveModule(
    Constants.OperatorConstants.RIGHT_BACK_SPEED_MOTOR_ID,      //can ID for speed
    Constants.OperatorConstants.RIGHT_BACK_DIRECTIION_MOTOR_ID,  //can ID for direction
    Constants.RIGHT_BACK_SPEED_IS_REVERSED,                     //boolean for if the speed motor is set to reverse 
    Constants.RIGHT_BACK_DIRECTION_IS_REVERSED,                 //boolean for if the direction motor is set to reverse
    Constants.OperatorConstants.RIGHT_BACK_CANCODER_ID,         //can ID for cancoder
    Constants.RIGHT_BACK_RADIAN_OFFSET,                         //to correct for encoder, so 0 is actually pointing ahead
    Constants.RIGHT_BACK_CANCODER_IS_REVERSED
  );

  private final Pigeon2 gyro = new Pigeon2(Constants.PIGEON_ID);

  //////////// Trying path planner 12-16-23
  //private SwerveDriveKinematics kinematics; // Trying Pathplanner


  // Return position of the swerve module for odometry
  public SwerveModulePosition[] getModulePosition(){
    return (new SwerveModulePosition[] {
      frontLeft.getPosition(),
      frontRight.getPosition(),
      backLeft.getPosition(),
      backRight.getPosition()
      });
    }

    // Return State of the swerve module for odometry 
  public SwerveModuleState[] getModuleStates(){
    return (new SwerveModuleState[] {
      frontLeft.getState(),
      frontRight.getState(),
      backLeft.getState(),
      backRight.getState()
      });
      
  }
/////////////////////////////////////////////////

  //Create Odometer for swerve drive
  private SwerveDriveOdometry odometer;

  //private final SwerveDrivePoseEstimator odometry;  // Odemeter for when we have vision to help drive the robot


  // Swerve subsystem constructor 
  public SwerveSubsystem() {
   
  //Restart robot encoders on startup
  resetAllEncoders();

  gyro.configFactoryDefault();
  zeroGyro();


  odometer = new SwerveDriveOdometry(Constants.kDriveKinematics,
    getHeadingRot2d(), 
    getModulePosition(),
    new Pose2d()); 

    //////////// Trying path planner 12-16-23
 // kinematics = new SwerveDriveKinematics(
 //     Constants.Swerve.flModuleOffset, 
  //    Constants.Swerve.frModuleOffset, 
  //    Constants.Swerve.blModuleOffset, 
  //    Constants.Swerve.brModuleOffset
  //  );

  // Configure AutoBuilder
  AutoBuilder.configureHolonomic(
    this::getPose, 
    this::resetOdometry, 
    this::getSpeeds, 
    this::driveRobotRelative, 
    Constants.Swerve.pathFollowerConfig, 
    this
  ); 

  
////////////////////////
  }

  public double getHeading()
  {
    //SmartDashboard.putNumber("Compass Heading", gyro.getAbsoluteCompassHeading());
    return Math.IEEEremainder(gyro.getYaw(), 360);
    //return gyro.getYaw();
  }

  public Rotation2d  getHeadingRot2d()
  {
    return Rotation2d.fromDegrees(gyro.getYaw());
  }

  public Rotation2d getRotation2d()
  {
    return Rotation2d.fromDegrees(getHeading());
  }

    // Get the location determined by the odometer.  Returns x and y location in meters
  public Pose2d getPose() {
    return odometer.getPoseMeters();
  }

  // Reset the odometer to a new location  **Note Pose2d contains transation2d (X, y coordinates on field and Rotation2d which is where robot is facing)
public void resetOdometry(Pose2d pose) {
    odometer.resetPosition(
      getRotation2d(),
      getModulePosition(), 
      pose);
}


public void resetAllEncoders()
{
 frontRight.resetEncoders();
 frontLeft.resetEncoders();
 backRight.resetEncoders();
 backLeft.resetEncoders();
 }

  public double  getRoll()
  {
    return (gyro.getRoll());
  }

  public void zeroGyro() {
    //gyro.setYaw(0);
    // gyroOffset = (DriverStation.getAlliance() == Alliance.Blue ? 0 : 180) % 360;
    gyro.setYaw(90);
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    SmartDashboard.putNumber("just yaw", gyro.getYaw());
    SmartDashboard.putNumber("Roll", gyro.getRoll());
    SmartDashboard.putNumber("Robot Yaw", getHeading());
    //SmartDashboard.putNumber("Compass Heading", gyro.getAbsoluteCompassHeading());
    SmartDashboard.putNumber("Left Front Encoder Rad", frontLeft.getAbsoluteEncoderRad());
    SmartDashboard.putNumber("Right Front Encoder Rad", frontRight.getAbsoluteEncoderRad());
    SmartDashboard.putNumber("Left Back Encoder Rad", backLeft.getAbsoluteEncoderRad());
    SmartDashboard.putNumber("Right Back Encoder Rad", backRight.getAbsoluteEncoderRad());
    
    SmartDashboard.putNumber("Left Front Angle", frontLeft.getAbsoluteEncoderAngle());
    SmartDashboard.putNumber("Right Front Angle", frontRight.getAbsoluteEncoderAngle());
    SmartDashboard.putNumber("Left Back Angle", backLeft.getAbsoluteEncoderAngle());
    SmartDashboard.putNumber("Right Back Angle", backRight.getAbsoluteEncoderAngle());

    SmartDashboard.putNumber("Left front distance in Meters", frontLeft.getSpeedPosition());
    SmartDashboard.putNumber("Right front distance in Meters", frontRight.getSpeedPosition());
    SmartDashboard.putNumber("Left back distance in Meters", backLeft.getSpeedPosition());
    SmartDashboard.putNumber("Right back distance in Meters", backRight.getSpeedPosition());

    SmartDashboard.putNumber("Left front direction position", frontLeft.getDirectionPosition());



    odometer.update(getRotation2d(), 
                    getModulePosition()
                  );
  }

  public void addPoseEstimatorSwerveMeasurement() {
    odometer.update(
      getRotation2d(),
      getModulePosition()
    );
  }

  public void stopModules()
  {
    frontLeft.stop();
    frontRight.stop();
    backLeft.stop();
    backRight.stop();
  }

  public void setModuleStates(SwerveModuleState[] desiredStates)
  {
    SwerveDriveKinematics.desaturateWheelSpeeds(desiredStates, Constants.physicalMaxSpeedMPS); //NormalizeWheelSpeeds doesn't exist, this is the closest thing idk
    frontLeft.setDesiredState(desiredStates[0]);
    frontRight.setDesiredState(desiredStates[1]);
    backLeft.setDesiredState(desiredStates[2]);
    backRight.setDesiredState(desiredStates[3]);
  }

 
 //////////// Trying path planner 12-16-23
  public ChassisSpeeds getSpeeds() {
    //return kinematics.toChassisSpeeds(getModuleStates());
    return Constants.kDriveKinematics.toChassisSpeeds(getModuleStates());
  }

  public void driveRobotRelative(ChassisSpeeds robotRelativeSpeeds) {
    //ChassisSpeeds targetSpeeds = ChassisSpeeds.fromFieldRelativeSpeeds(robotRelativeSpeeds, getRotation2d());
    ChassisSpeeds targetSpeeds = ChassisSpeeds.discretize(robotRelativeSpeeds, .02);

    //SwerveModuleState[] targetStates = kinematics.toSwerveModuleStates(targetSpeeds);
    SwerveModuleState[] targetStates = Constants.kDriveKinematics.toSwerveModuleStates(targetSpeeds);
    setModuleStates(targetStates);
  }
////////////////////////////////////////
}

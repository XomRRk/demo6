package org.example.demo6;

import javafx.beans.property.*;
import java.sql.Timestamp;

public class TestResult {

    private final StringProperty serialNumber;
    private final ObjectProperty<Timestamp> testDate;
    private final DoubleProperty directTravel;
    private final DoubleProperty additionalTravel;
    private final DoubleProperty differentialTravel;
    private final DoubleProperty actuationForceDirect;
    private final DoubleProperty actuationForceReverse;
    private final BooleanProperty noSticking;
    private final DoubleProperty movementSpeed;
    private final DoubleProperty voltageDropNo;
    private final DoubleProperty voltageDropNc;
    private final StringProperty status;
    private final StringProperty switchType;

    public TestResult(String serialNumber, Timestamp testDate, double directTravel, double additionalTravel,
                      double differentialTravel, double actuationForceDirect, double actuationForceReverse,
                      boolean noSticking, double movementSpeed, double voltageDropNo, double voltageDropNc,
                      String status, String switchType) {

        this.serialNumber = new SimpleStringProperty(serialNumber);
        this.testDate = new SimpleObjectProperty<>(testDate);
        this.directTravel = new SimpleDoubleProperty(directTravel);
        this.additionalTravel = new SimpleDoubleProperty(additionalTravel);
        this.differentialTravel = new SimpleDoubleProperty(differentialTravel);
        this.actuationForceDirect = new SimpleDoubleProperty(actuationForceDirect);
        this.actuationForceReverse = new SimpleDoubleProperty(actuationForceReverse);
        this.noSticking = new SimpleBooleanProperty(noSticking);
        this.movementSpeed = new SimpleDoubleProperty(movementSpeed);
        this.voltageDropNo = new SimpleDoubleProperty(voltageDropNo);
        this.voltageDropNc = new SimpleDoubleProperty(voltageDropNc);
        this.status = new SimpleStringProperty(status);
        this.switchType = new SimpleStringProperty(switchType);
    }

    public StringProperty serialNumberProperty() { return serialNumber; }
    public ObjectProperty<Timestamp> testDateProperty() { return testDate; }
    public DoubleProperty directTravelProperty() { return directTravel; }
    public DoubleProperty additionalTravelProperty() { return additionalTravel; }
    public DoubleProperty differentialTravelProperty() { return differentialTravel; }
    public DoubleProperty actuationForceDirectProperty() { return actuationForceDirect; }
    public DoubleProperty actuationForceReverseProperty() { return actuationForceReverse; }
    public BooleanProperty noStickingProperty() { return noSticking; }
    public DoubleProperty movementSpeedProperty() { return movementSpeed; }
    public DoubleProperty voltageDropNoProperty() { return voltageDropNo; }
    public DoubleProperty voltageDropNcProperty() { return voltageDropNc; }
    public StringProperty statusProperty() { return status; }
    public StringProperty switchTypeProperty() { return switchType; }
}

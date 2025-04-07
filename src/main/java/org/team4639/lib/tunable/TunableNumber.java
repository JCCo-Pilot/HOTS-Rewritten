package org.team4639.lib.tunable;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import org.team4639.lib.RobotModes;

/**
 * Class for a tunable number. Gets value from dashboard in tuning mode, returns default if not or
 * value not in dashboard.
 *
 * @author elliot
 * @author njona
 */
public class TunableNumber {
  private final String key;
  private double defaultValue = 0;

  /**
   * Create a new TunableNumber
   *
   * @param dashboardKey Key on dashboard
   */
  public TunableNumber(String dashboardKey) {
    this.key = dashboardKey;
  }

  /**
   * Get the default value for the number that has been set
   *
   * @return The default value
   */
  public double getDefault() {
    return defaultValue;
  }

  /**
   * Set the default value of the number
   *
   * @param defaultValue The default value
   */
  public void setDefaultValue(double defaultValue) {
    this.defaultValue = defaultValue;

    // If the SmartDashboard already has a value for this key, use that; otherwise,
    // use the default value provided
    double newValue = SmartDashboard.getNumber(key, defaultValue);
    // Update the value on SmartDashboard.
    SmartDashboard.putNumber(key, newValue);
  }

  /**
   * Set the default value of the number
   *
   * @param defaultValue The default value
   */
  public TunableNumber withDefaultValue(double defaultValue) {
    this.setDefaultValue(defaultValue);
    return this;
  }

  /**
   * Get the current value, from dashboard if available and in tuning mode
   *
   * @return The current value
   */
  public double get() {
    if (RobotModes.isTuningMode()) return SmartDashboard.getNumber(key, defaultValue);
    else return defaultValue;
  }

  private static final Collection<Runnable> listeners = new ArrayList<>();

  /**
   * Adds a listener to be run whenever the TunableNumber's value changes. The listener will also be
   * called a single time immediately with the current value. If this behavior is not desired, use
   * {@link TunableNumber#onChange(Consumer, boolean)} instead.
   *
   * @param onChange The listener. This will be called with the new value every time the
   *     TunableNumber changes
   */
  public TunableNumber onChange(Consumer<Double> onChange) {
    return onChange(onChange, true);
  }

  /**
   * Adds a listener to be run whenever the TunableNumber's value changes.
   *
   * @param onChange The listener. This will be called with the new value every time the
   *     TunableNumber changes
   * @param shouldCallImmediately Whether to call the listener a single time immediately when
   *     registered.
   */
  public TunableNumber onChange(Consumer<Double> onChange, boolean shouldCallImmediately) {
    if (shouldCallImmediately) {
      onChange.accept(get());
    }
    // Periodically check if the value of the tunableNumber has changed
    // and if so, call the listener.
    synchronized (listeners) {
      listeners.add(
          new Runnable() {
            private double oldValue = get();

            @Override
            public void run() {
              double newValue = get();
              if (newValue != oldValue) {
                System.out.println(key + ":" + newValue + "<-" + oldValue);
                onChange.accept(newValue);
              }
              oldValue = newValue;
            }
          });
    }

    return this;
  }

  /**
   * Adds a listener to be run whenever any of the given TunableNumbers' values changes. The
   * listener will also be called a single time immediately with the current value. If this behavior
   * is not desired, use {@link TunableNumber#onAnyChange(Consumer, boolean, TunableNumber...)}
   * instead
   *
   * @param onChange The listener. This will be called with an array containing the new values every
   *     time any of the given TunableNumbers' values changes
   */
  public static void onAnyChange(Consumer<double[]> onChange, TunableNumber... tunableNumbers) {
    onAnyChange(onChange, true, tunableNumbers);
  }

  /**
   * Adds a listener to be run whenever any of the given TunableNumbers' values changes.
   *
   * @param onChange The listener. This will be called with an array containing the new values every
   *     time any of the given TunableNumbers' values changes
   * @param shouldCallImmediately Whether to call the listener a single time immediately when
   *     registered.
   */
  public static void onAnyChange(
      Consumer<double[]> onChange, boolean shouldCallImmediately, TunableNumber... tunableNumbers) {
    for (int j = 0; j < tunableNumbers.length; j++) {
      TunableNumber tunableNumber = tunableNumbers[j];
      tunableNumber.onChange(
          (ignored) -> {
            double[] values = new double[tunableNumbers.length];
            for (int i = 0; i < tunableNumbers.length; i++) {
              values[i] = tunableNumber.get();
            }
            onChange.accept(values);
          },
          j == 0 && shouldCallImmediately);
    }
  }

  // Hook into the event loop to run the tunableNumberListeners.
  // This way, there is no need to remember to call an update function
  // in Robot.java or other places.
  static {
    //noinspection resource
    //        new Notifier(
    //            () -> {
    //                listeners.forEach(Runnable::run);
    //                System.out.println("Run");
    //            }
    //        ).startPeriodic(0.02);
    new SubsystemBase() {
      @Override
      public void periodic() {
        synchronized (listeners) {
          listeners.forEach(Runnable::run);
        }
      }
    };
  }
}

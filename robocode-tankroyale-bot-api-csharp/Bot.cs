using System;
using System.Threading;

namespace Robocode.TankRoyale.BotApi
{
  public class Bot : BaseBot, IBot
  {
    readonly __BotInternals __botInternals;

    public Bot() : base()
    {
      __botInternals = new __BotInternals(this);
    }

    public Bot(BotInfo botInfo) : base(botInfo)
    {
      __botInternals = new __BotInternals(this);
    }

    public Bot(BotInfo botInfo, Uri serverUri) : base(botInfo, serverUri)
    {
      __botInternals = new __BotInternals(this);
    }

    public bool IsRunning => __botInternals.IsRunning;

    public void SetForward(double distance)
    {
      if (Double.IsNaN(distance))
      {
        throw new ArgumentException("distance cannot be NaN");
      }
      __botInternals.distanceRemaining = distance;
    }

    public void Forward(double distance)
    {
      SetForward(distance);
      Go();
      __botInternals.AwaitMovementComplete();
    }

    public void SetBack(double distance)
    {
      if (Double.IsNaN(distance))
      {
        throw new ArgumentException("distance cannot be NaN");
      }
      __botInternals.distanceRemaining = -distance;
    }

    public void Back(double distance)
    {
      SetBack(distance);
      Go();
      __botInternals.AwaitMovementComplete();
    }

    public double DistanceRemaining => __botInternals.distanceRemaining;

    public void SetMaxSpeed(double maxSpeed)
    {
      if (maxSpeed < 0)
      {
        maxSpeed = 0;
      }
      else if (maxSpeed > ((IBot)this).MaxSpeed)
      {
        maxSpeed = ((IBot)this).MaxSpeed;
      }
      __botInternals.maxSpeed = maxSpeed;
    }

    public void SetTurnLeft(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      __botInternals.turnRemaining = degrees;
    }

    public void TurnLeft(double degrees)
    {
      SetTurnLeft(degrees);
      Go();
      __botInternals.AwaitTurnComplete();
    }

    public void SetTurnRight(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      __botInternals.turnRemaining = -degrees;
    }

    public void TurnRight(double degrees)
    {
      SetTurnRight(degrees);
      Go();
      __botInternals.AwaitTurnComplete();
    }

    public double TurnRemaining => __botInternals.turnRemaining;

    public void SetMaxTurnRate(double maxTurnRate)
    {
      if (maxTurnRate < 0)
      {
        maxTurnRate = 0;
      }
      else if (maxTurnRate > ((IBot)this).MaxTurnRate)
      {
        maxTurnRate = ((IBot)this).MaxTurnRate;
      }
      __botInternals.maxTurnRate = maxTurnRate;
    }

    public void SetTurnGunLeft(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      __botInternals.gunTurnRemaining = degrees;
    }

    public void TurnGunLeft(double degrees)
    {
      SetTurnGunLeft(degrees);
      Go();
      __botInternals.awaitGunTurnComplete();
    }

    public void SetTurnGunRight(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      __botInternals.gunTurnRemaining = -degrees;
    }

    public void TurnGunRight(double degrees)
    {
      SetTurnGunRight(degrees);
      Go();
      __botInternals.awaitGunTurnComplete();
    }

    public double GunTurnRemaining => __botInternals.gunTurnRemaining;

    public void SetMaxGunTurnRate(double maxGunTurnRate)
    {
      if (maxGunTurnRate < 0)
      {
        maxGunTurnRate = 0;
      }
      else if (maxGunTurnRate > ((IBot)this).MaxGunTurnRate)
      {
        maxGunTurnRate = ((IBot)this).MaxGunTurnRate;
      }
      __botInternals.maxGunTurnRate = maxGunTurnRate;
    }

    public void SetTurnRadarLeft(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      __botInternals.radarTurnRemaining = degrees;
    }

    public void TurnRadarLeft(double degrees)
    {
      SetTurnRadarLeft(degrees);
      Go();
      __botInternals.awaitRadarTurnComplete();
    }

    public void SetTurnRadarRight(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      __botInternals.radarTurnRemaining = -degrees;
    }

    public void TurnRadarRight(double degrees)
    {
      SetTurnRadarRight(degrees);
      Go();
      __botInternals.awaitRadarTurnComplete();
    }

    public double RadarTurnRemaining => __botInternals.radarTurnRemaining;

    public void SetMaxRadarTurnRate(double maxRadarTurnRate)
    {
      if (maxRadarTurnRate < 0)
      {
        maxRadarTurnRate = 0;
      }
      else if (maxRadarTurnRate > ((IBot)this).MaxRadarTurnRate)
      {
        maxRadarTurnRate = ((IBot)this).MaxRadarTurnRate;
      }
      __botInternals.maxRadarTurnRate = maxRadarTurnRate;
    }

    public void Fire(double firepower)
    {
      Firepower = firepower;
      Go();
    }

    internal class __BotInternals
    {
      private double absDeceleration;

      private IBot parent;

      internal double maxSpeed;
      internal double maxTurnRate;
      internal double maxGunTurnRate;
      internal double maxRadarTurnRate;

      internal double distanceRemaining;
      internal double turnRemaining;
      internal double gunTurnRemaining;
      internal double radarTurnRemaining;

      private bool isCollidingWithBot;
      private bool isOverDriving;

      private int turnNumber;

      internal Thread thread;
      private EventWaitHandle isBlocked = new ManualResetEvent(false);
      private volatile bool isRunning;

      public __BotInternals(IBot parent)
      {
        this.parent = parent;

        this.absDeceleration = Math.Abs(parent.Deceleration);

        this.maxSpeed = parent.MaxForwardSpeed;
        this.maxTurnRate = parent.MaxTurnRate;
        this.maxGunTurnRate = parent.MaxGunTurnRate;
        this.maxRadarTurnRate = parent.MaxRadarTurnRate;

        var internals = ((BaseBot)parent).__internals;

        internals.onDisconnectedManager.Add((OnDisconnected));
        internals.onGameEndedManager.Add(OnGameEnded);
        internals.onSkippedTurnManager.Add(OnSkippedTurn);
        internals.onHitBotManager.Add(OnHitBot);
        internals.onHitWallManager.Add(OnHitWall);
        internals.onTickManager.Add(OnTick);
        internals.onDeathManager.Add(OnDeath);
      }

      internal bool IsRunning
      {
        get => isRunning;
      }

      private void OnTick(TickEvent evt)
      {
        turnNumber = evt.TurnNumber;
        ProcessTurn();
      }

      private void OnDisconnected(DisconnectedEvent evt)
      {
        StopThread();
      }

      private void OnGameEnded(GameEndedEvent evt)
      {
        StopThread();
      }

      private void OnSkippedTurn(SkippedTurnEvent evt)
      {
        ProcessTurn();
      }

      private void OnHitBot(BotHitBotEvent evt)
      {
        if (evt.Rammed)
        {
          distanceRemaining = 0;
        }
        isCollidingWithBot = true;
      }

      private void OnHitWall(BotHitWallEvent evt)
      {
        distanceRemaining = 0;
      }

      private void OnDeath(BotDeathEvent evt)
      {
        if (evt.VictimId == parent.MyId)
        {
          StopThread();
        }
      }

      private void ProcessTurn()
      {
        // No movement is possible, when the bot has become disabled
        if (parent.IsDisabled)
        {
          distanceRemaining = 0;
          turnRemaining = 0;
        }
        UpdateHeadings();
        UpdateMovement();
        isCollidingWithBot = false;

        // If this is the first turn -> Call the run method on the Bot class
        if (turnNumber == 1)
        {
          StopThread();

          distanceRemaining = 0;
          turnRemaining = 0;
          gunTurnRemaining = 0;
          radarTurnRemaining = 0;
          isCollidingWithBot = false;
          isOverDriving = false;

          StartThread();
        }

        // Unblock waiting methods
        lock (isBlocked)
        {
          parent.Go();
          isBlocked.Set();
        }
      }

      private void StartThread()
      {
        thread = new Thread(new ThreadStart(parent.Run));
        thread.Start();
        isRunning = true;
      }

      private void StopThread()
      {
        if (thread != null)
        {
          isRunning = false;
          thread.Interrupt();
          try
          {
            thread.Join();
          }
          catch (ThreadInterruptedException)
          {
          }
          thread = null;
        }
      }

      /** Updates the bot heading, gun heading, and radar heading. */
      private void UpdateHeadings()
      {
        if (!isCollidingWithBot)
        {
          UpdateTurnRemaining();
        }
        UpdateGunTurnRemaining();
        UpdateRadarTurnRemaining();
      }

      private void UpdateTurnRemaining()
      {
        double absTurnRate = Math.Abs(parent.TurnRate);

        double turnRate = Math.Min(absTurnRate, parent.CalcMaxTurnRate(parent.Speed));
        if (parent.TurnRemaining < 0)
        {
          turnRate *= -1;
        }
        if (Math.Abs(parent.TurnRemaining) < absTurnRate)
        {
          if (parent.IsAdjustGunForBodyTurn)
          {
            gunTurnRemaining -= parent.TurnRemaining;
          }
          turnRemaining = 0;
        }
        else
        {
          if (parent.IsAdjustGunForBodyTurn)
          {
            gunTurnRemaining -= turnRate;
          }
          turnRemaining -= turnRate;
        }
        if (turnRemaining > 0)
        {
          parent.TurnRate = Math.Min(maxTurnRate, turnRemaining);
        }
        else
        {
          parent.TurnRate = Math.Max(-maxTurnRate, turnRemaining);
        }
      }

      private void UpdateGunTurnRemaining()
      {
        double absGunTurnRate = Math.Abs(parent.GunTurnRate);

        if (Math.Abs(gunTurnRemaining) < absGunTurnRate)
        {
          if (parent.IsAdjustRadarForGunTurn)
          {
            radarTurnRemaining -= gunTurnRemaining;
          }
          gunTurnRemaining = 0;
        }
        else
        {
          if (parent.IsAdjustRadarForGunTurn)
          {
            radarTurnRemaining -= parent.GunTurnRate;
          }
          gunTurnRemaining -= parent.GunTurnRate;
        }
        if (gunTurnRemaining > 0)
        {
          parent.GunTurnRate = Math.Min(maxGunTurnRate, gunTurnRemaining);
        }
        else
        {
          parent.GunTurnRate = Math.Max(-maxGunTurnRate, gunTurnRemaining);
        }
      }

      private void UpdateRadarTurnRemaining()
      {
        double absRadarTurnRate = Math.Abs(parent.RadarTurnRate);

        if (Math.Abs(parent.RadarTurnRemaining) < absRadarTurnRate)
        {
          radarTurnRemaining = 0;
        }
        else
        {
          radarTurnRemaining -= parent.RadarTurnRate;
        }
        if (radarTurnRemaining > 0)
        {
          parent.RadarTurnRate = Math.Min(maxRadarTurnRate, radarTurnRemaining);
        }
        else
        {
          parent.RadarTurnRate = Math.Max(-maxRadarTurnRate, radarTurnRemaining);
        }
      }

      // This is Nat Pavasants method described here:
      // http://robowiki.net/wiki/User:Positive/Optimal_Velocity#Nat.27s_updateMovement
      private void UpdateMovement()
      {
        double distance = distanceRemaining;
        if (Double.IsNaN(distance))
        {
          distance = 0;
        }

        var speed = GetNewSpeed(parent.Speed, distance);
        parent.TargetSpeed = speed;

        // If we are over-driving our distance and we are now at velocity=0 then we stopped
        if (IsNearZero(speed) && isOverDriving)
        {
          distanceRemaining = 0;
          distance = 0;
          isOverDriving = false;
        }

        // If we are moving normally and the breaking distance is more than remaining distance, enable
        // the overdrive flag
        if (Math.Sign(distance * speed) != -1)
        {
          isOverDriving = GetDistanceTraveledUntilStop(speed) > Math.Abs(distance);
        }

        distanceRemaining = distance - speed;
      }

      /// <summary>
      /// Returns the new speed based on the current speed and distance to move.
      ///
      /// <param name="speed">Is the current speed</param>
      /// <param name="distance">Is the distance to move</param>
      /// <return>The new speed</return>
      //
      // Credits for this algorithm goes to Patrick Cupka (aka Voidious), Julian Kent (aka
      // Skilgannon), and Positive:
      // http://robowiki.net/wiki/User:Voidious/Optimal_Velocity#Hijack_2
      private double GetNewSpeed(double speed, double distance)
      {
        if (distance < 0)
        {
          // If the distance is negative, then change it to be positive and change the sign of the
          // input velocity and the result
          return -GetNewSpeed(-speed, -distance);
        }

        double targetSpeed;
        if (distance == Double.PositiveInfinity)
        {
          targetSpeed = maxSpeed;
        }
        else
        {
          targetSpeed = Math.Min(GetMaxSpeed(distance), maxSpeed);
        }

        if (speed >= 0)
        {
          return Math.Max(speed - absDeceleration, Math.Min(targetSpeed, speed + parent.Acceleration));
        } // else
        return Math.Max(speed - parent.Acceleration, Math.Min(targetSpeed, speed + GetMaxDeceleration(-speed)));
      }

      private double GetMaxSpeed(double distance)
      {
        var decelTime =
            Math.Max(
                1,
                Math.Ceiling( // sum of 0... decelTime, solving for decelTime using quadratic formula
                    (Math.Sqrt((4 * 2 / absDeceleration) * distance + 1) - 1) / 2));

        if (decelTime == Double.PositiveInfinity)
        {
          return parent.MaxSpeed;
        }

        var decelDist =
            (decelTime / 2)
                * (decelTime - 1) // sum of 0..(decelTime-1)
                * absDeceleration;

        return ((decelTime - 1) * absDeceleration) + ((distance - decelDist) / decelTime);
      }

      private double GetMaxDeceleration(double speed)
      {
        var decelTime = speed / absDeceleration;
        var accelTime = (1 - decelTime);

        return Math.Min(1, decelTime) * absDeceleration + Math.Max(0, accelTime) * parent.Acceleration;
      }

      private double GetDistanceTraveledUntilStop(double speed)
      {
        speed = Math.Abs(speed);
        double distance = 0;
        while (speed > 0)
        {
          distance += (speed = GetNewSpeed(speed, 0));
        }
        return distance;
      }

      private bool IsNearZero(double value) => Math.Abs(value) < .00001;

      internal void AwaitMovementComplete()
      {
        Await(() => distanceRemaining == 0);
      }

      internal void AwaitTurnComplete()
      {
        Await(() => turnRemaining == 0);
      }

      internal void awaitGunTurnComplete()
      {
        Await(() => gunTurnRemaining == 0);
      }

      internal void awaitRadarTurnComplete()
      {
        Await(() => radarTurnRemaining == 0);
      }

      private void Await(Func<bool> condition)
      {
        lock (isBlocked)
        {
          while (IsRunning && condition.Invoke())
          {
            isBlocked.WaitOne();
            isBlocked.Reset();
          }
        }
      }
    }
  }
}
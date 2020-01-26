namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Individual bot results.
  /// </summary>
  public sealed class BotResults
  {
    /// <summary>Identifier of the bot used in this battle.</summary>
    int Id { get; }

    /// <summary>Rank/placement of the bot, where 1 is 1st place, 4 is 4th place etc.</summary>
    int Rank { get; }

    /// <summary>Survival score gained whenever another bot is defeated.</summary>
    double Survival { get; }

    /// <summary>Last survivor score as last survivor in a round.</summary>
    double LastSurvivorBonus { get; }

    /// <summary>Bullet damage given.</summary>
    double BulletDamage { get; }

    /// <summary>Bullet kill bonus.</summary>
    double BulletKillBonus { get; }

    /// <summary>Ram damage given.</summary>
    double RamDamage { get; }

    /// <summary>Ram kill bonus.</summary>
    double RamKillBonus { get; }

    /// <summary>Total score.</summary>
    double TotalScore { get; }

    /// <summary>Number of 1st places.</summary>
    double FirstPlaces { get; }

    /// <summary>Number of 2nd places.</summary>
    double SecondPlaces { get; }

    /// <summary>Number of 3rd places.</summary>
    double ThirdPlaces { get; }

    public BotResults(int id, int rank, double survival, double lastSurvivorBonus, double bulletDamage,
      double bulletKillBonus, double ramDamage, double ramKillBonus, double totalScore, double firstPlaces,
      double secondPlaces, double thirdPlaces)
    {
      Id = id;
      Rank = rank;
      Survival = survival;
      LastSurvivorBonus = lastSurvivorBonus;
      BulletDamage = bulletDamage;
      BulletKillBonus = bulletKillBonus;
      RamDamage = ramDamage;
      RamKillBonus = ramKillBonus;
      TotalScore = totalScore;
      FirstPlaces = firstPlaces;
      SecondPlaces = secondPlaces;
      ThirdPlaces = thirdPlaces;
    }
  }
}
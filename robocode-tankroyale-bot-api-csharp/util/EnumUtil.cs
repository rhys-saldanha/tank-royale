using System.Linq;
using System.Runtime.Serialization;

namespace Robocode.TankRoyale.BotApi
{
  public sealed class EnumUtil
  {
    public static string GetEnumMemberAttrValue<T>(T enumVal)
    {
      var enumType = typeof(T);
      var memInfo = enumType.GetMember(enumVal.ToString());

      var attr = memInfo.FirstOrDefault()?.GetCustomAttributes(false).OfType<EnumMemberAttribute>().FirstOrDefault();
      return (attr != null) ? attr.Value : null;
    }
  }
}
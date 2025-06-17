package sharpmc.pl.objects.rewards;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

public class RewardSerializer implements ObjectSerializer<Reward> {

    @Override
    public boolean supports(@NonNull Class<? super Reward> type) {
        return Reward.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull Reward object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.add("name", object.getName());
        data.add("chance", object.getChance());
        data.addCollection("commands", object.getCommands(), String.class);
    }

    @Override
    public Reward deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        return new Reward(
                data.get("name", String.class),
                data.get("chance", Double.class),
                data.getAsList("commands", String.class)
        );
    }
}

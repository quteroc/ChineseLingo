package com.chineselingo.user.mapper;
import com.chineselingo.user.UserState;
import com.chineselingo.user.dto.UserStateDto;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.*;

public final class UserStateMapper {

    private UserStateMapper() {}

    public static UserStateDto toDto(UserState state) {
        if (state == null) {
            return null;
        }

        UserStateDto dto = new UserStateDto();

        // BitSet -> List<Integer>
        BitSet knownCharsCopy = state.getKnownChars() != null ? (BitSet) state.getKnownChars().clone() : new BitSet();
        dto.setKnownChars(bitSetToList(knownCharsCopy));

        // fastutil map -> java map
        Map<Integer, UserState.ReviewHistory> map = new HashMap<>();
        map.putAll(state.getReviewHistoryMap());
        dto.setReviewHistory(map);

        return dto;
    }

    public static UserState fromDto(UserStateDto dto) {
        if (dto == null) {
            return new UserState();
        }

        BitSet knownChars = dto.getKnownChars() != null ? listToBitSet(dto.getKnownChars()) : new BitSet();

        Int2ObjectOpenHashMap<UserState.ReviewHistory> history = new Int2ObjectOpenHashMap<>();

        if (dto.getReviewHistory() != null) {
            dto.getReviewHistory().forEach((charId, rh) -> {
                history.put(
                        charId,
                        new UserState.ReviewHistory(
                                rh.getViews(),
                                rh.getSuccesses(),
                                rh.getLastReviewedEpochSeconds()
                        )
                );
            });
        }

        return UserState.restore(knownChars, history);
    }

    private static List<Integer> bitSetToList(BitSet bitSet) {
        List<Integer> result = new ArrayList<>();
        for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet.nextSetBit(i + 1)) {
            result.add(i);
        }
        return result;
    }

    private static BitSet listToBitSet(List<Integer> list) {
        BitSet bitSet = new BitSet();
        if (list != null) {
            list.forEach(bitSet::set);
        }
        return bitSet;
    }
}
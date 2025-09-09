package net.swimmingtuna.lotm.nihilums.tweaks.EventManager;

import java.util.List;

public interface IEventsCapabilityData {
   int INITIAL_CAPACITY = 300;

   String TAG_LIST_R_KEY = "events_listR_capability";
   String TAG_LIST_R_TO_DELETE_KEY = "events_listR_to_delete_capability";

   String TAG_LIST_W_KEY = "events_listW_capability";
   String TAG_LIST_W_TO_DELETE_KEY = "events_listW_to_delete_capability";

   List<IFunction> getRegularEvents();
   List<IFunction> getWorldEvents();

   void markDeleteR(IFunction func);
   void addR(IFunction func);

   void markDeleteW(IFunction func);
   void addW(IFunction func);

   void deleteAllMarked();
}

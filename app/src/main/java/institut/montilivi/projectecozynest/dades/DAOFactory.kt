package institut.montilivi.projectecozynest.dades

import android.content.Context
import institut.montilivi.projectecozynest.firestore.ManegadorFirestore

object DAOFactory {
    enum class TipusBBDD{
        FIREBASE
    }
    fun obtenUsuarisDAO(context: Context?, tipusBBDD: TipusBBDD):UsuarisDAO{
        return when(tipusBBDD){
            TipusBBDD.FIREBASE -> UsuarisDAOFirebaseImpl(ManegadorFirestore())
        }
    }
    fun obtenValoracionsDAO(context: Context?, tipusBBDD: TipusBBDD):ValoracionsDAO{
        return when(tipusBBDD){
            TipusBBDD.FIREBASE -> ValoracionsDAOFirebaseImpl(ManegadorFirestore())
        }
    }
    fun obtenChatsDAO(context: Context?, tipusBBDD: TipusBBDD):ChatsDAO{
        return when(tipusBBDD){
            TipusBBDD.FIREBASE -> ChatsDAOFirebaseImpl(ManegadorFirestore())
        }
    }
    fun obtenMissatgesDAO(context: Context?, tipusBBDD: TipusBBDD):MissatgesDAO{
        return when(tipusBBDD){
            TipusBBDD.FIREBASE -> MissatgesDAOFirebaseImpl(ManegadorFirestore())
        }
    }
}
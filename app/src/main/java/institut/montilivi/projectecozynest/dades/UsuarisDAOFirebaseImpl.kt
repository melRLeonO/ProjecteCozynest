package institut.montilivi.projectecozynest.dades

import android.util.Log
import institut.montilivi.projectecozynest.firestore.ManegadorFirestore
import institut.montilivi.projectecozynest.model.Estudiant
import institut.montilivi.projectecozynest.model.PersonaGran
import institut.montilivi.projectecozynest.model.Resposta
import institut.montilivi.projectecozynest.model.UsuariBase
import kotlinx.coroutines.tasks.await

class UsuarisDAOFirebaseImpl(private val db: ManegadorFirestore) : UsuarisDAO {

    override suspend fun obtenUsuaris(): Resposta<List<UsuariBase>> {
        return try {
            val refUsuaris = db.firestoreDB.collection(db.USUARIS)
            val consulta = refUsuaris.get().await()
            val usuaris = mutableListOf<UsuariBase>()

            for (document in consulta.documents) {
                val usuariMap = document.data ?: continue
                val usuari = when (usuariMap["rol"]) {
                    "Estudiant" -> {
                        Estudiant(
                            id = document.id,
                            correu = usuariMap["correu"] as String,
                            nom = usuariMap["nom"] as String,
                            cognoms = usuariMap["cognoms"] as String,
                            dataNaixement = usuariMap["dataNaixement"] as String,
                            genere = usuariMap["genere"] as String,
                            fotoPerfil = usuariMap["fotoPerfil"] as String,
                            rol = "Estudiant",
                            disponibilitat = usuariMap["disponibilitat"] as Map<String, List<Boolean>>,
                            tipusHabitacio = usuariMap["tipusHabitacio"] as String,
                            preferenciaTipusAjuda = usuariMap["preferenciaTipusAjuda"] as String,
                            duradaEstada = usuariMap["duradaEstada"] as String,
                            habilitatsLlar = usuariMap["habilitatsLlar"] as List<String>,
                            preferenciesConvivencia = usuariMap["preferenciesConvivencia"] as List<String>
                        )
                    }
                    "Persona Gran" -> {
                        PersonaGran(
                            id = document.id,
                            correu = usuariMap["correu"] as String,
                            nom = usuariMap["nom"] as String,
                            cognoms = usuariMap["cognoms"] as String,
                            dataNaixement = usuariMap["dataNaixement"] as String,
                            genere = usuariMap["genere"] as String,
                            fotoPerfil = usuariMap["fotoPerfil"] as String,
                            rol = "Persona Gran",
                            imatgesAllotjament = usuariMap["imatgesAllotjament"] as String,
                            ubicacioAllotjament = usuariMap["ubicacioAllotjament"] as String,
                            preuAllotjament = usuariMap["preuAllotjament"] as Int,
                            superficieAllotjament = usuariMap["superficieAllotjament"] as String,
                            descripcioAllotjament = usuariMap["descripcioAllotjament"] as String,
                            tipusAjudaSolicitada = usuariMap["tipusAjudaSolicitada"] as List<String>,
                            preferenciesConvivencia = usuariMap["preferenciesConvivencia"] as List<String>,
                            latitud = (usuariMap["latitud"] as? Double) ?: 0.0,
                            longitud = (usuariMap["longitud"] as? Double) ?: 0.0
                        )
                    }
                    else -> {
                        UsuariBase(
                            id = document.id,
                            correu = usuariMap["correu"] as String,
                            nom = usuariMap["nom"] as String,
                            cognoms = usuariMap["cognoms"] as String,
                            dataNaixement = usuariMap["dataNaixement"] as String,
                            genere = usuariMap["genere"] as String,
                            fotoPerfil = usuariMap["fotoPerfil"] as String,
                            rol = "Desconegut"
                        )
                    }
                }
                usuaris.add(usuari)
            }
            Resposta.Exit(usuaris)
        } catch (e: Exception) {
            Log.e("UserRegistration", "Error obtenint usuaris: ${e.message}")
            Resposta.Fracas(e.message ?: "Error en obtenir els usuaris")
        }
    }
    override suspend fun obtenUsuari(id: String): Resposta<UsuariBase> {
        var usuari: UsuariBase = UsuariBase()
        try {
            val refUsuaris = db.firestoreDB.collection(db.USUARIS)
            val refUsuari = refUsuaris.document(id)
            val document = refUsuari.get().await()

            usuari = when (val rol = document.getString("rol")) {
                "Estudiant" -> document.toObject(Estudiant::class.java) ?: Estudiant()
                "Persona Gran" -> document.toObject(PersonaGran::class.java) ?: PersonaGran()
                else -> UsuariBase()
            }
        } catch (e: Exception) {
            return Resposta.Fracas(e.message ?: "Error buscando el usuario $id")
        }
        return Resposta.Exit(usuari)
    }
    override suspend fun afegeixUsuari(usuari: UsuariBase): Resposta<Boolean> {
        return try {
            val refUsuaris = db.firestoreDB.collection(db.USUARIS)

            val usuariMap = when (usuari) {
                is Estudiant -> {
                    val map = mapOf(
                        "id" to usuari.id,
                        "correu" to usuari.correu,
                        "nom" to usuari.nom,
                        "cognoms" to usuari.cognoms,
                        "dataNaixement" to usuari.dataNaixement,
                        "genere" to usuari.genere,
                        "fotoPerfil" to usuari.fotoPerfil,
                        "rol" to usuari.rol,
                        "disponibilitat" to usuari.disponibilitat,
                        "tipusHabitacio" to usuari.tipusHabitacio,
                        "preferenciaTipusAjuda" to usuari.preferenciaTipusAjuda,
                        "duradaEstada" to usuari.duradaEstada,
                        "habilitatsLlar" to usuari.habilitatsLlar,
                        "preferenciesConvivencia" to usuari.preferenciesConvivencia
                    )
                    Log.d("UserRegistration", "Creating Estudiant user: $map")
                    map
                }
                is PersonaGran -> {
                    val map = mapOf(
                        "id" to usuari.id,
                        "correu" to usuari.correu,
                        "nom" to usuari.nom,
                        "cognoms" to usuari.cognoms,
                        "dataNaixement" to usuari.dataNaixement,
                        "genere" to usuari.genere,
                        "fotoPerfil" to usuari.fotoPerfil,
                        "rol" to usuari.rol,
                        "imatgesAllotjament" to usuari.imatgesAllotjament,
                        "ubicacioAllotjament" to usuari.ubicacioAllotjament,
                        "preuAllotjament" to usuari.preuAllotjament,
                        "superficieAllotjament" to usuari.superficieAllotjament,
                        "descripcioAllotjament" to usuari.descripcioAllotjament,
                        "tipusAjudaSolicitada" to usuari.tipusAjudaSolicitada,
                        "preferenciesConvivencia" to usuari.preferenciesConvivencia,
                        "latitud" to usuari.latitud,
                        "longitud" to usuari.longitud
                    )

                    Log.d("UserRegistration", "Creating PersonaGran user: $map")
                    map
                }
                else -> {
                    val map = mapOf(
                        "id" to usuari.id,
                        "correu" to usuari.correu,
                        "nom" to usuari.nom,
                        "cognoms" to usuari.cognoms,
                        "dataNaixement" to usuari.dataNaixement,
                        "genere" to usuari.genere,
                        "fotoPerfil" to usuari.fotoPerfil,
                        "rol" to usuari.rol
                    )
                    Log.d("UserRegistration", "Creating generic user: $map")
                    map
                }
            }

            Log.d("UserRegistration", "Checking if user exists with email: ${usuari.correu}")
            val consulta = refUsuaris.whereEqualTo("correu", usuari.correu).get().await()
            if (consulta.documents.isNotEmpty()) {
                Log.d("UserRegistration", "User already exists, skipping save.")
                return Resposta.Exit(false)
            }
            val refUsuariNou = refUsuaris.document()
            val usuariMapComplet = usuariMap.toMutableMap()
            usuariMapComplet["id"] = refUsuariNou.id
            Log.d("UserRegistration", "Saving new user to Firestore: $usuariMapComplet")
            refUsuariNou.set(usuariMapComplet).await()

            Log.d("UserRegistration", "User saved successfully with ID: ${refUsuariNou.id}")
            Resposta.Exit(true)
        } catch (e: Exception) {
            Log.e("UserRegistration", "Error in user registration: ${e.message}")
            Resposta.Fracas(e.message ?: "Error en alta d'usuari ${usuari.correu}")
        }
    }
    override suspend fun modificaUsuari(usuari: UsuariBase): Resposta<Boolean> {
        return try {
            val refUsuaris = db.firestoreDB.collection(db.USUARIS)

            // Buscar el documento del usuario por su id
            val consulta = refUsuaris.document(usuari.id).get().await()

            if (consulta.exists()) {
                // Crear mapa con los datos del usuario a modificar
                val usuariMap = when (usuari) {
                    is Estudiant -> {
                        mapOf(
                            "correu" to usuari.correu,
                            "nom" to usuari.nom,
                            "cognoms" to usuari.cognoms,
                            "dataNaixement" to usuari.dataNaixement,
                            "genere" to usuari.genere,
                            "fotoPerfil" to usuari.fotoPerfil,
                            "rol" to usuari.rol,
                            "disponibilitat" to usuari.disponibilitat,
                            "tipusHabitacio" to usuari.tipusHabitacio,
                            "preferenciaTipusAjuda" to usuari.preferenciaTipusAjuda,
                            "duradaEstada" to usuari.duradaEstada,
                            "habilitatsLlar" to usuari.habilitatsLlar,
                            "preferenciesConvivencia" to usuari.preferenciesConvivencia
                        )
                    }
                    is PersonaGran -> {
                        mapOf(
                            "id" to usuari.id,
                            "correu" to usuari.correu,
                            "nom" to usuari.nom,
                            "cognoms" to usuari.cognoms,
                            "dataNaixement" to usuari.dataNaixement,
                            "genere" to usuari.genere,
                            "fotoPerfil" to usuari.fotoPerfil,
                            "rol" to usuari.rol,
                            "imatgesAllotjament" to usuari.imatgesAllotjament,
                            "ubicacioAllotjament" to usuari.ubicacioAllotjament,
                            "preuAllotjament" to usuari.preuAllotjament,
                            "superficieAllotjament" to usuari.superficieAllotjament,
                            "descripcioAllotjament" to usuari.descripcioAllotjament,
                            "tipusAjudaSolicitada" to usuari.tipusAjudaSolicitada,
                            "preferenciesConvivencia" to usuari.preferenciesConvivencia,
                            "latitud" to usuari.latitud,
                            "longitud" to usuari.longitud
                        )

                    }
                    else -> {
                        mapOf(
                            "correu" to usuari.correu,
                            "nom" to usuari.nom,
                            "cognoms" to usuari.cognoms,
                            "dataNaixement" to usuari.dataNaixement,
                            "genere" to usuari.genere,
                            "fotoPerfil" to usuari.fotoPerfil,
                            "rol" to usuari.rol
                        )
                    }
                }

                // Actualizar los datos del usuario en Firestore
                refUsuaris.document(usuari.id).update(usuariMap).await()
                Resposta.Exit(true)
            } else {
                Log.d("UserRegistration", "User not found, cannot update.")
                Resposta.Exit(false)
            }
        } catch (e: Exception) {
            Log.e("UserRegistration", "Error modificant usuari: ${e.message}")
            Resposta.Fracas(e.message ?: "Error en modificar l'usuari ${usuari.id}")
        }
    }
    override suspend fun existeixUsuari(correu: String): Resposta<Boolean> {
        var consultaBuida = false
        try {
            val refUsuaris = db.firestoreDB.collection(db.USUARIS)
            val consulta = refUsuaris.whereEqualTo("correu", correu)
            consultaBuida = consulta.get().await().documents.isEmpty()

        }catch (e: Exception) {
            Resposta.Fracas(e.message ?: "Error cercant l'usuari $correu" )
        }
        return Resposta.Exit(!consultaBuida)
    }
}
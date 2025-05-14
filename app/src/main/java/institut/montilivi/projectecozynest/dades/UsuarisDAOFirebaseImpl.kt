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
            Log.e("UserRegistration", "Error obtaining users: ${e.message}")
            Resposta.Fracas(e.message ?: "Error obtaining users")
        }
    }
    override suspend fun obtenUsuari(id: String): Resposta<UsuariBase> {
        var usuari: UsuariBase = UsuariBase()
        try {
            val refUsuaris = db.firestoreDB.collection(db.USUARIS)
            val document = refUsuaris.document(id).get().await()

            if (!document.exists()) {
                return Resposta.Fracas("User not found")
            }

            usuari = when (val rol = document.getString("rol")) {
                "Estudiant" -> document.toObject(Estudiant::class.java) ?: Estudiant()
                "Persona Gran" -> document.toObject(PersonaGran::class.java) ?: PersonaGran()
                else -> UsuariBase()
            }
        } catch (e: Exception) {
            return Resposta.Fracas(e.message ?: "Error searching for user $id")
        }
        return Resposta.Exit(usuari)
    }

    override suspend fun obtenUsuariPerCorreu(correu: String): Resposta<UsuariBase> {
        try {
            val refUsuaris = db.firestoreDB.collection(db.USUARIS)
            val documents = refUsuaris.whereEqualTo("correu", correu).get().await().documents

            if (documents.isEmpty()) {
                return Resposta.Fracas("User not found")
            }

            val document = documents.first()
            val usuari = when (val rol = document.getString("rol")) {
                "Estudiant" -> document.toObject(Estudiant::class.java) ?: Estudiant()
                "Persona Gran" -> document.toObject(PersonaGran::class.java) ?: PersonaGran()
                else -> UsuariBase()
            }

            return Resposta.Exit(usuari)
        } catch (e: Exception) {
            return Resposta.Fracas(e.message ?: "Error searching for user with email $correu")
        }
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
            Resposta.Fracas(e.message ?: "Error in user registration ${usuari.correu}")
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
            Log.e("UserRegistration", "Error modifying user: ${e.message}")
            Resposta.Fracas(e.message ?: "Error modifying user ${usuari.id}")
        }
    }
    override suspend fun existeixUsuari(correu: String): Resposta<Boolean> {
        var consultaBuida = false
        try {
            val refUsuaris = db.firestoreDB.collection(db.USUARIS)
            val consulta = refUsuaris.whereEqualTo("correu", correu)
            consultaBuida = consulta.get().await().documents.isEmpty()

        }catch (e: Exception) {
            Resposta.Fracas(e.message ?: "Error searching for user $correu" )
        }
        return Resposta.Exit(!consultaBuida)
    }

    override suspend fun bloquejaUsuari(idUsuariQueBloqueja: String, idUsuariBloquejat: String): Resposta<Boolean> {
        return try {
            val refBloquejos = db.firestoreDB.collection(db.BLOQUEJOS)
            
            // Verificar si ya existe el bloqueo
            val consulta = refBloquejos
                .whereEqualTo("idUsuariQueBloqueja", idUsuariQueBloqueja)
                .whereEqualTo("idUsuariBloquejat", idUsuariBloquejat)
                .get()
                .await()

            if (consulta.documents.isNotEmpty()) {
                return Resposta.Exit(true) // Ya está bloqueado
            }

            // Crear nuevo bloqueo
            val bloqueig = mapOf(
                "idUsuariQueBloqueja" to idUsuariQueBloqueja,
                "idUsuariBloquejat" to idUsuariBloquejat,
                "dataBloqueig" to com.google.firebase.Timestamp.now()
            )

            refBloquejos.add(bloqueig).await()
            Resposta.Exit(true)
        } catch (e: Exception) {
            Resposta.Fracas(e.message ?: "Error blocking user")
        }
    }

    override suspend fun desbloquejaUsuari(idUsuariQueDesbloqueja: String, idUsuariDesbloquejat: String): Resposta<Boolean> {
        return try {
            val refBloquejos = db.firestoreDB.collection(db.BLOQUEJOS)
            
            // Buscar el documento del bloqueo
            val consulta = refBloquejos
                .whereEqualTo("idUsuariQueBloqueja", idUsuariQueDesbloqueja)
                .whereEqualTo("idUsuariBloquejat", idUsuariDesbloquejat)
                .get()
                .await()

            if (consulta.documents.isEmpty()) {
                return Resposta.Exit(true) // No estaba bloqueado
            }

            // Eliminar el bloqueo
            for (document in consulta.documents) {
                document.reference.delete().await()
            }

            Resposta.Exit(true)
        } catch (e: Exception) {
            Resposta.Fracas(e.message ?: "Error unblocking user")
        }
    }

    override suspend fun estaBloquejat(idUsuariQueComprova: String, idUsuariAComprovar: String): Resposta<Boolean> {
        return try {
            val refBloquejos = db.firestoreDB.collection(db.BLOQUEJOS)
            
            // Buscar si existe un bloqueo en cualquier dirección
            val consulta1 = refBloquejos
                .whereEqualTo("idUsuariQueBloqueja", idUsuariQueComprova)
                .whereEqualTo("idUsuariBloquejat", idUsuariAComprovar)
                .get()
                .await()

            val consulta2 = refBloquejos
                .whereEqualTo("idUsuariQueBloqueja", idUsuariAComprovar)
                .whereEqualTo("idUsuariBloquejat", idUsuariQueComprova)
                .get()
                .await()

            // Si existe un bloqueo en cualquier dirección, devolver true
            Resposta.Exit(consulta1.documents.isNotEmpty() || consulta2.documents.isNotEmpty())
        } catch (e: Exception) {
            Resposta.Fracas(e.message ?: "Error checking if user is blocked")
        }
    }

    override suspend fun obtenirUsuarisPerNom(nom: String, correuActual: String): Resposta<List<UsuariBase>> {
        return try {
            val refUsuaris = db.firestoreDB.collection(db.USUARIS)
            val refBloquejos = db.firestoreDB.collection(db.BLOQUEJOS)
            
            // Primero obtenemos el usuario actual para saber su rol
            val usuariActualDoc = refUsuaris.whereEqualTo("correu", correuActual).get().await().documents.firstOrNull()
            val usuariActual = usuariActualDoc?.toObject(UsuariBase::class.java)

            if (usuariActual == null) {
                return Resposta.Exit(emptyList())
            }

            val rolOposat = if (usuariActual.rol == "Estudiant") "Persona Gran" else "Estudiant"

            // Obtener todos los bloqueos donde el usuario actual es bloqueador o bloqueado
            val bloquejosQueFa = refBloquejos.whereEqualTo("idUsuariQueBloqueja", usuariActual.id).get().await().documents
            val bloquejosQueRep = refBloquejos.whereEqualTo("idUsuariBloquejat", usuariActual.id).get().await().documents
            val idsBloquejats = bloquejosQueFa.mapNotNull { it.getString("idUsuariBloquejat") }
            val idsQueEmBloquegen = bloquejosQueRep.mapNotNull { it.getString("idUsuariQueBloqueja") }
            val idsExclosos = idsBloquejats + idsQueEmBloquegen

            // Obtenemos los usuarios del rol opuesto que coincidan con el nombre
            val resultats = refUsuaris.whereEqualTo("rol", rolOposat).get().await()
            
            val usuaris = resultats.documents.mapNotNull {
                when (it.getString("rol")) {
                    "Estudiant" -> it.toObject(Estudiant::class.java)
                    "Persona Gran" -> it.toObject(PersonaGran::class.java)
                    else -> null
                }
            }.filter { usuari ->
                usuari.correu != correuActual &&
                usuari.nom.contains(nom, ignoreCase = true) &&
                usuari.id !in idsExclosos
            }

            Resposta.Exit(usuaris)
        } catch (e: Exception) {
            Resposta.Fracas(e.message ?: "Error obtaining users by name")
        }
    }
}
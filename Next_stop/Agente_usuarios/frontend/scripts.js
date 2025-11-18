// =====================
// VARIABLES GLOBALES
// =====================
const chatBox = document.getElementById("chat-box");
const mensajeInput = document.getElementById("mensaje");
const enviarBtn = document.getElementById("enviar");

// =====================
// FUNCIÓN PRINCIPAL PARA MOSTRAR RESPUESTAS
// =====================
function mostrarRespuesta(response) {
    console.log("📥 Respuesta recibida del servidor:", response);

    // NUEVO: Si es un objeto individual, renderizar como card
    if (response.type === "cards" && typeof response.data === "object" && !Array.isArray(response.data)) {
        const modulo = response.modulo || "general";
        const item = response.data;  // El objeto individual

        const card = document.createElement("div");
        card.className = `mensaje agente card-respuesta card-${modulo}`;

        if (modulo === "notificaciones") {
            card.innerHTML = `
                <h3>🔔 Notificación</h3>
                <p><b>Mensaje:</b> ${item.mensaje}</p>
                <p><b>Tipo:</b> ${item.tipo}</p>
                <p><b>Estado:</b> ${item.estado}</p>
            `;
        } else if (modulo === "lugares") {
            card.innerHTML = `
                <h3>🏝️ ${item.name}</h3>
                <p><b>Descripción:</b> ${item.description || "Sin descripción"}</p>
                <p><b>Estado:</b> ${item.status}</p>
            `;
        } else {
            // Fallback para otros módulos
            card.innerHTML = `<pre>${JSON.stringify(item, null, 2)}</pre>`;
        }

        chatBox.appendChild(card);
    }
    // Si vienen tarjetas (respuesta estructurada) - arrays
    else if (response.type === "cards" && Array.isArray(response.data)) {
        const modulo = response.modulo || "general";

        response.data.forEach(item => {
            const card = document.createElement("div");
            card.className = `mensaje agente card-respuesta card-${modulo}`;

            if (modulo === "lugares") {
                card.innerHTML = `
                    <h3>🏝️ ${item.name}</h3>
                    <p><b>Descripción:</b> ${item.description || "Sin descripción"}</p>
                    <p><b>Estado:</b> ${item.status}</p>
                `;
            } else if (modulo === "notificaciones") {
                card.innerHTML = `
                    <h3>🔔 ${item.tipo || "General"}</h3>
                    <p><b>Mensaje:</b> ${item.mensaje}</p>
                    <p><b>Estado:</b> ${item.estado}</p>
                `;
            } else {
                // fallback general
                card.innerHTML = `<pre>${JSON.stringify(item, null, 2)}</pre>`;
            }

            chatBox.appendChild(card);
        });
    }
    // Si la respuesta es texto simple
    else if (response.type === "text") {
        const div = document.createElement("div");
        div.className = "mensaje agente";
        div.textContent = response.data;
        chatBox.appendChild(div);
    }
    // Si hay error
    else if (response.type === "error" || response.error) {
        const div = document.createElement("div");
        div.className = "mensaje agente error";
        div.textContent = `⚠️ Error: ${response.data || response.error}`;
        chatBox.appendChild(div);
    }
    // Cualquier otro caso
    else {
        const div = document.createElement("div");
        div.className = "mensaje agente";
        div.textContent = JSON.stringify(response, null, 2);
        chatBox.appendChild(div);
    }

    chatBox.scrollTop = chatBox.scrollHeight;
}

// =====================
// FUNCIÓN PARA ENVIAR MENSAJE
// =====================
async function enviarMensaje() {
    const mensaje = mensajeInput.value.trim();
    if (!mensaje) return;

    // Mostrar mensaje del usuario
    const divUsuario = document.createElement("div");
    divUsuario.className = "mensaje usuario";
    divUsuario.textContent = mensaje;
    chatBox.appendChild(divUsuario);

    mensajeInput.value = "";
    chatBox.scrollTop = chatBox.scrollHeight;

    try {
        const response = await fetch("http://127.0.0.1:9001/chat", {  // Puerto del bridge_usuarios
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ message: mensaje })
        });

        const data = await response.json();
        mostrarRespuesta(data);
    } catch (error) {
        console.error("❌ Error al conectar con el servidor:", error);
        mostrarRespuesta({ type: "error", data: "Error al conectar con el servidor" });
    }
}

// =====================
// EVENTOS
// =====================
enviarBtn.addEventListener("click", enviarMensaje);
mensajeInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter") enviarMensaje();
});

// =====================
// BOTONES RÁPIDOS
// =====================
document.querySelectorAll(".quick-btn").forEach(btn => {
    btn.addEventListener("click", () => {
        mensajeInput.value = btn.textContent;
        enviarMensaje();
    });
});

// =====================
// BÚSQUEDAS RECIENTES
// =====================
document.querySelectorAll(".recent-searches button").forEach(btn => {
    btn.addEventListener("click", () => {
        mensajeInput.value = btn.textContent;
        enviarMensaje();
    });
});

// =====================
// ESTADO DEL USUARIO
// =====================
async function actualizarEstadoUsuario() {
    try {
        const res = await fetch("http://127.0.0.1:9001/estado");
        const estado = await res.json();

        document.querySelector(".estado-usuario").innerHTML = `
            <p>Acceso limitado: <span>${estado.acceso || 'Sí'}</span></p>
            <p>Lugares visibles: <span>${estado.lugares_visibles || 'Aceptados'}</span></p>
            <p>Notificaciones: <span>${estado.notificaciones || 'Disponibles'}</span></p>
        `;
    } catch (error) {
        document.querySelector(".estado-usuario").innerHTML = `<p>⚠️ No se pudo conectar al servidor</p>`;
    }
}

setInterval(actualizarEstadoUsuario, 10000);
actualizarEstadoUsuario();

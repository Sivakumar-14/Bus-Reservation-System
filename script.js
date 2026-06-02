function toggleForm(formType) {
    if (formType === 'register') {
        document.getElementById("loginBox").style.display = "none";
        document.getElementById("registerBox").style.display = "block";
    } else {
        document.getElementById("loginBox").style.display = "block";
        document.getElementById("registerBox").style.display = "none";
    }
}

function handleRegister() {
    const data = {
        action: "register",
        username: document.getElementById("regUser").value,
        password: document.getElementById("regPass").value
    };
    const msgBox = document.getElementById("messageBoxRegister");
    
    fetch('/BusReservationSystem/BusServlet', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
    .then(res => res.json())
    .then(result => {
        msgBox.innerText = result.message;
        msgBox.style.color = result.status === "success" ? "green" : "red";
        if(result.status === "success") {
            setTimeout(() => toggleForm('login'), 2000);
        }
    }).catch(err => { msgBox.innerText = "Connection Error!"; msgBox.style.color = "red"; });
}

function handleLogin() {
    const data = {
        action: "login",
        username: document.getElementById("loginUser").value,
        password: document.getElementById("loginPass").value,
        role: document.getElementById("loginRole").value
    };
    const msgBox = document.getElementById("messageBoxLogin");

    fetch('/BusReservationSystem/BusServlet', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
    .then(res => res.json())
    .then(result => {
        if (result.status === "success") {
            if (result.role === "USER") window.location.href = "user.html";
            else if (result.role === "ADMIN") window.location.href = "admin.html";
        } else {
            msgBox.innerText = result.message;
            msgBox.style.color = "red";
        }
    }).catch(err => { msgBox.innerText = "Login connection failed!"; msgBox.style.color = "red"; });
}

function updatePrice() {
    const start = document.getElementById("startingPoint").value;
    const dest = document.getElementById("destination").value;
    let price = start === dest ? 0 : ((start === "Chennai" && dest === "Coimbatore") || (start === "Coimbatore" && dest === "Chennai") ? 1850 : 1400);
    document.getElementById("priceDisplay").innerText = price;
}

function handleBooking() {
    const data = {
        action: "book",
        passengerName: document.getElementById("passName").value,
        passengerEmail: document.getElementById("passEmail").value,
        busNo: document.getElementById("busNo").value,
        startingPoint: document.getElementById("startingPoint").value,
        destination: document.getElementById("destination").value,
        date: document.getElementById("bookDate").value,
        time: document.getElementById("bookTime").value,
        seatNo: document.getElementById("seatNo").value,
        price: document.getElementById("priceDisplay").innerText
    };
    const msgBox = document.getElementById("messageBox");

    fetch('/BusReservationSystem/BusServlet', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
    .then(res => res.json())
    .then(result => {
        msgBox.innerText = result.message;
        msgBox.style.color = result.status === "success" ? "green" : "red";
        if (result.status === "success") {
            alert("🎉 " + result.message);
            document.getElementById("bookingForm").reset();
            updatePrice();
        }
    }).catch(err => { msgBox.innerText = "Server booking exception error!"; msgBox.style.color = "red"; });
}

function handleCancellation() {
    const data = {
        action: "cancel",
        bookingNo: document.getElementById("cancelBookingNo").value
    };
    const msgBox = document.getElementById("messageBox");

    fetch('/BusReservationSystem/BusServlet', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
    .then(res => res.json())
    .then(result => {
        msgBox.innerText = result.message;
        msgBox.style.color = result.status === "success" ? "green" : "red";
        if (result.status === "success") {
            alert("❌ " + result.message);
            document.getElementById("cancelBookingNo").value = "";
        }
    }).catch(err => { msgBox.innerText = "Cancellation exception!"; msgBox.style.color = "red"; });
}

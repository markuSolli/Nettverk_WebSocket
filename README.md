# Nettverksprogrammering - Øving 6
Obligatorisk innlevering i IDATT2104 Nettverksprogrammering.  
Server-klassen kjører en HTTP-server på port 3000 som leverer index.html.
WebSocket-klassen lytter på port 3001 og håndterer klienter i en egen tråd. Dette er en egen implementasjon av WebSocket.
I nettsiden index.html kan man tegne sammen med alle andre klienter som er på nettsiden. Frontend sender korte WebSocket-meldinger til tjeneren når brukeren tegner, disse blir videresendt til alle tilkoblede klienter.

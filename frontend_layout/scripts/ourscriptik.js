const dataUrl = 'http://localhost:8080/graph/status'; // это ссылка на сервер, могут быть параметры в ней, чтобы отсечь ненужные записи или попросить сервер сделать сортировку 
let tableDataPromise = fetch(dataUrl);
	tableDataPromise.then((response) => {
		console.log(response);
		return response.json();
		
	}).then((jsonData) => {
		let ourTable = document.getElementById('cool_table_id').getElementsByTagName('tbody');
		let apples = jsonData; //apples = JSON.parse(apple).sort((a,b)=> a.n > b.n),
		let i = 1;

		for (let apple of apples) {
			let ourTr = document.createElement('tr');

			let ourTd = document.createElement('td');
			ourTd.innerText = i;
			ourTr.appendChild(ourTd);
			ourTd = document.createElement('td');
			ourTd.innerText = apple.graphName;
			ourTr.appendChild(ourTd);
			ourTd = document.createElement('td');
			ourTd.innerText = apple.state;
			ourTr.appendChild(ourTd);
			ourTd = document.createElement('td');
			ourTd.innerText = apple.author;
			ourTr.appendChild(ourTd);
			ourTd = document.createElement('td');
			ourTd.innerText = apple.start;
			ourTr.appendChild(ourTd);
			ourTd = document.createElement('td');
			ourTd.innerText = apple.end;
			ourTr.appendChild(ourTd);
			ourTd = document.createElement('td');
			ourTd.innerText = apple.tags;
			ourTr.appendChild(ourTd);

			ourTable[0].appendChild(ourTr);
			i++;
		}
	}).catch (e => console.error(e));

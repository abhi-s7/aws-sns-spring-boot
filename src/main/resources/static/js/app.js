// Create Topic
async function createTopic(event) {
    event.preventDefault();
    const topicName = document.getElementById('topicName').value;
    const resultDiv = document.getElementById('createTopicResult');

    try {
        const response = await axios.post(`/api/sns/create-topic?topicName=${topicName}`);
        resultDiv.className = 'result';
        resultDiv.textContent = `${response.data.message} - Please wait a few seconds for the topic to appear in the list.`;
        document.getElementById('createTopicForm').reset();
        listTopics();
    } catch (error) {
        resultDiv.className = 'result error';
        resultDiv.textContent = error.response?.data || 'Failed to create topic';
    }
}

// List Topics
async function listTopics() {
    const resultDiv = document.getElementById('topicListResult');
    const topicListDiv = document.getElementById('topicList');

    // Show loading state
    resultDiv.className = 'result';
    resultDiv.textContent = 'Loading topics...';
    topicListDiv.innerHTML = '<div class="empty-state">Please wait...</div>';

    try {
        const response = await axios.get('/api/sns/list-topics');
        resultDiv.className = 'result';
        
        if (response.data.length === 0) {
            topicListDiv.innerHTML = '<div class="empty-state">No topics found</div>';
            resultDiv.textContent = 'No topics available';
        } else {
            resultDiv.textContent = `Found ${response.data.length} topic(s)`;
            topicListDiv.innerHTML = response.data.map(arn => 
                `<div class="queue-item">${arn}</div>`
            ).join('');
        }
    } catch (error) {
        resultDiv.className = 'result error';
        resultDiv.textContent = error.response?.data || 'Failed to list topics';
        topicListDiv.innerHTML = '';
    }
}

// Subscribe Email
async function subscribeEmail(event) {
    event.preventDefault();
    const topicArn = document.getElementById('subscribeTopicArn').value;
    const email = document.getElementById('email').value;
    const resultDiv = document.getElementById('subscribeResult');

    try {
        const response = await axios.post(`/api/sns/subscribe?topicArn=${encodeURIComponent(topicArn)}&email=${encodeURIComponent(email)}`);
        resultDiv.className = 'result';
        resultDiv.textContent = response.data.message;
        document.getElementById('subscribeForm').reset();
    } catch (error) {
        resultDiv.className = 'result error';
        resultDiv.textContent = error.response?.data || 'Failed to subscribe';
    }
}

// Delete Topic
async function deleteTopic(event) {
    event.preventDefault();
    const topicArn = document.getElementById('deleteTopicArn').value;
    const resultDiv = document.getElementById('deleteTopicResult');

    if (!confirm('Are you sure you want to delete this topic?')) {
        return;
    }

    try {
        const response = await axios.delete(`/api/sns/delete-topic?topicArn=${encodeURIComponent(topicArn)}`);
        resultDiv.className = 'result';
        resultDiv.textContent = response.data + ' - Please wait up to 60 seconds for the topic to disappear from the list.';
        document.getElementById('deleteTopicForm').reset();
        listTopics();
    } catch (error) {
        resultDiv.className = 'result error';
        resultDiv.textContent = error.response?.data || 'Failed to delete topic';
    }
}

// Load topics on page load
window.onload = () => {
    listTopics();
};
